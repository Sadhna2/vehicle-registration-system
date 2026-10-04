import { useEffect, useState } from "react";
import { api, label, money, today } from "../api";

export default function FeeSchedule() {
  const [fees, setFees] = useState(null);
  const [failed, setFailed] = useState(false);
  useEffect(() => {
    let active = true;
    api("/fees")
      .then((rows) => {
        const rules = new Map();
        rows
          .filter(
            (rule) =>
              rule.applicationType === "NEW" && rule.effectiveFrom <= today(),
          )
          .sort(
            (a, b) =>
              b.effectiveFrom.localeCompare(a.effectiveFrom) ||
              b.feeRuleId - a.feeRuleId,
          )
          .forEach((rule) => {
            if (!rules.has(rule.vehicleCategory))
              rules.set(rule.vehicleCategory, rule);
          });
        if (active) setFees([...rules.values()]);
      })
      .catch(() => {
        if (active) setFailed(true);
      });
    return () => {
      active = false;
    };
  }, []);
  return (
    <aside className="fee-schedule mb-4">
      <span className="eyebrow">CURRENT REGISTRATION FEES</span>
      <div className="d-flex flex-wrap gap-3 mt-2">
        {failed ? (
          <span>
            Fee information is unavailable. Your fee is calculated when the
            application is submitted.
          </span>
        ) : fees === null ? (
          <span>Loading fee information…</span>
        ) : fees.length === 0 ? (
          <span>
            No current fee rules are available. Please contact your RTO
            administrator before applying.
          </span>
        ) : (
          fees.map((fee) => (
            <span key={fee.feeRuleId}>
              {label(fee.vehicleCategory)}{" "}
              <strong>{money(fee.feeAmount)}</strong>
            </span>
          ))
        )}
      </div>
    </aside>
  );
}