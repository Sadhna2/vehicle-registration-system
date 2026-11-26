// The supporting documents every new registration needs (FR-1), and the
// file rules the backend enforces: PDF, JPG or PNG, at most 5 MB.
export const REQUIRED_DOCUMENTS = [
  { type: "INVOICE", title: "Purchase invoice" },
  { type: "INSURANCE", title: "Insurance certificate" },
  { type: "IDENTITY_PROOF", title: "Identity proof" },
];

export const MAX_DOCUMENT_BYTES = 5 * 1024 * 1024;
export const ACCEPTED_FILES = ".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png";

export function documentTitle(type) {
  return REQUIRED_DOCUMENTS.find((document) => document.type === type)?.title ?? type;
}

// Returns an error message for a file the backend would refuse, or "" if it looks fine.
// The backend still checks the real file contents.
export function checkDocumentFile(file) {
  if (!file) return "";
  if (!/\.(pdf|jpe?g|png)$/i.test(file.name)) return "Choose a PDF, JPG or PNG file.";
  if (file.size === 0) return "This file is empty.";
  if (file.size > MAX_DOCUMENT_BYTES) return "This file is larger than 5 MB.";
  return "";
}

export function fileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

// Summarises an application's documents for the workflow buttons and progress bar.
export function documentSummary(documents = []) {
  const byType = Object.fromEntries(documents.map((document) => [document.documentType, document]));
  const required = REQUIRED_DOCUMENTS.map(({ type }) => byType[type]);
  return {
    byType,
    allUploaded: required.every(Boolean),
    allVerified: required.every((document) => document?.status === "VERIFIED"),
    anyRejected: required.some((document) => document?.status === "REJECTED"),
  };
}
