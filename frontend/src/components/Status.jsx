import { label } from "../api";
export default function Status({ value }) {
  const color =
    value === "APPROVED"
      ? "success"
      : value === "REJECTED"
        ? "danger"
        : value === "CORRECTION_REQUIRED"
          ? "warning"
          : "primary";
  return <span className={`badge text-bg-${color}`}>{label(value)}</span>;
}
