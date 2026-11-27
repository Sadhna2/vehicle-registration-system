import { clearSession, sessionToken } from "./session";

// Sends a request to the backend with the signed-in user's JWT and turns error
// responses into readable messages. A 401 means the session is no longer valid,
// so the stored session is cleared and the app returns to the sign-in page.
async function send(path, { method = "GET", body, headers = {}, timeoutMs = 20000 } = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);
  const token = sessionToken();
  let response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      credentials: "omit",
      signal: controller.signal,
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...headers,
      },
      body,
    });
  } catch (failure) {
    throw new Error(
      failure.name === "AbortError"
        ? "The service is taking too long to respond. Please try again."
        : "The registration service is unavailable. Please try again shortly.",
    );
  } finally {
    clearTimeout(timeout);
  }
  if (!response.ok) {
    const text = await response.text();
    let problem = null;
    try {
      problem = text ? JSON.parse(text) : null;
    } catch {
      problem = null;
    }
    if (response.status === 401 && token) clearSession();
    const fieldErrors = Array.isArray(problem?.errors)
      ? problem.errors.map((field) => `${field.field}: ${field.message}`).join(". ")
      : "";
    const error = new Error(
      fieldErrors ||
        problem?.detail ||
        problem?.message ||
        (response.status >= 500
          ? "The service could not complete this request. Please try again shortly."
          : response.status === 401
            ? "Invalid email or password."
            : response.status === 403
              ? "Your account does not have access to this action."
              : response.status === 413
                ? "The file is too large."
                : `This request could not be completed (${response.status}).`),
    );
    error.status = response.status;
    error.code = problem?.code;
    throw error;
  }
  return response;
}

async function json(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return normalize(JSON.parse(text));
  } catch {
    throw new Error("The service returned an unexpected response.");
  }
}

export async function api(path, method = "GET", data) {
  const response = await send(path, {
    method,
    headers: {
      Accept: "application/json",
      ...(data === undefined ? {} : { "Content-Type": "application/json" }),
    },
    ...(data === undefined ? {} : { body: JSON.stringify(data) }),
  });
  return json(response);
}

// Uploads a file as multipart/form-data. The browser sets the multipart boundary,
// so no Content-Type header is given here.
export async function upload(path, fields) {
  const form = new FormData();
  Object.entries(fields).forEach(([name, value]) => form.append(name, value));
  const response = await send(path, {
    method: "POST",
    headers: { Accept: "application/json" },
    body: form,
    timeoutMs: 120000,
  });
  return json(response);
}

// Downloads a protected file with the JWT and opens it in a new browser tab.
// If the browser blocks the new tab, the file is saved as a download instead,
// so the user never leaves the application page.
export async function viewFile(path) {
  const viewer = window.open("", "_blank");
  try {
    const response = await send(path, { timeoutMs: 120000 });
    const url = URL.createObjectURL(await response.blob());
    if (viewer) {
      viewer.location.href = url;
    } else {
      const link = document.createElement("a");
      link.href = url;
      link.download = fileNameFrom(response.headers.get("Content-Disposition"));
      document.body.appendChild(link);
      link.click();
      link.remove();
    }
    setTimeout(() => URL.revokeObjectURL(url), 60000);
  } catch (failure) {
    viewer?.close();
    throw failure;
  }
}

// Downloads a protected file (CSV export or PDF) with the JWT and saves it
// under the file name the server suggests.
export async function downloadFile(path) {
  const response = await send(path, { timeoutMs: 120000 });
  const url = URL.createObjectURL(await response.blob());
  const link = document.createElement("a");
  link.href = url;
  link.download = fileNameFrom(response.headers.get("Content-Disposition"));
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 60000);
}

function fileNameFrom(disposition) {
  const encoded = /filename\*=UTF-8''([^;]+)/i.exec(disposition || "");
  if (encoded) {
    try {
      return decodeURIComponent(encoded[1]);
    } catch {
      // fall through to the plain filename
    }
  }
  return /filename="?([^";]+)"?/i.exec(disposition || "")?.[1] || "document";
}

export const money = (value) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(
    value ?? 0,
  );
export const label = (value) => (value || "").replaceAll("_", " ");
// Every application status, in workflow order (used by the status search).
export const APPLICATION_STATUSES = [
  "SUBMITTED",
  "UNDER_VERIFICATION",
  "CORRECTION_REQUIRED",
  "INSPECTION_PENDING",
  "APPROVED",
  "REJECTED",
];
export const categories = [
  "TWO_WHEELER",
  "PRIVATE_CAR",
  "THREE_WHEELER",
  "MVMG",
  "LMV",
];
export const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

function normalize(value) {
  if (Array.isArray(value)) return value.map(normalize);
  if (!value || typeof value !== "object") return value;
  const row = Object.fromEntries(
    Object.entries(value).map(([key, item]) => [key, normalize(item)]),
  );
  if (row.applicationRefNo && row.applicationStatus)
    return {
      ...row,
      reference: row.applicationRefNo,
      type: row.applicationType,
      status: row.applicationStatus,
      amount: row.payableAmount,
      submittedAt: row.submittedDate,
      updatedAt: row.updatedDate,
      vehicleDetails: row.vehicle,
      vehicle:
        `${row.vehicle?.manufacturerName || ""} ${row.vehicle?.modelName || ""}`.trim(),
      owner:
        `${row.applicant?.firstName || ""} ${row.applicant?.lastName || ""}`.trim(),
      inspectionDate: row.inspectionScheduleDate,
      paid: row.payment?.paymentStatus === "SUCCESS",
    };
  if (row.paymentId)
    return {
      ...row,
      amount: row.amountPaid,
      lastFour: row.cardLastFourDigit,
      reference: row.transactionReferenceId,
    };
  return row;
}
