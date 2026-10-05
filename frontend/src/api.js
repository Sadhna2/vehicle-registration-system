export async function api(path, method = "GET", data) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20000);
  let response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      credentials: "omit",
      signal: controller.signal,
      headers: {
        Accept: "application/json",
        ...(data === undefined ? {} : { "Content-Type": "application/json" }),
      },
      ...(data === undefined ? {} : { body: JSON.stringify(data) }),
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
  const text = await response.text();
  let body = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      if (response.ok)
        throw new Error("The service returned an unexpected response.");
    }
  }
  if (!response.ok) {
    const fieldErrors = Array.isArray(body?.errors)
      ? body.errors
          .map((field) => `${field.field}: ${field.message}`)
          .join(". ")
      : "";
    const error = new Error(
      fieldErrors ||
        body?.detail ||
        body?.message ||
        (response.status >= 500
          ? "The service could not complete this request. Please try again shortly."
          : response.status === 401
            ? "Invalid email or password."
            : response.status === 403
              ? "Your account does not have access to this action."
              : `This request could not be completed (${response.status}).`),
    );
    error.status = response.status;
    error.code = body?.code;
    throw error;
  }
  return normalize(body);
}

export const money = (value) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(
    value ?? 0,
  );
export const label = (value) => (value || "").replaceAll("_", " ");
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
