import { useState } from "react";
import { api, label, money, today } from "../api";
import Status from "./Status";
import Form from "./Form";
import VehicleForm from "./VehicleForm";
import Progress from "./Progress";
export default function Detail({ data, actor, run, busy, onChange, onClose }) {
  const [remarks, setRemarks] = useState("");
  const [appointment, setAppointment] = useState(today());
  const [certificate, setCertificate] = useState(data.certificate);
  const owner = actor.role === "OWNER";
  const applicant = owner && actor.id === data.applicant.ownerId;
  const reviewer = ["RTO_OFFICER", "RTO_ADMIN"].includes(actor.role);
  const closed = ["APPROVED", "REJECTED"].includes(data.status);
  const act = (path, method, body) =>
    run(async () =>
      onChange(
        await api(
          `/applications/${encodeURIComponent(data.reference)}/${path}`,
          method,
          body,
        ),
      ),
    );
  const actions =
    data.status === "SUBMITTED"
      ? ["START", "REJECT"]
      : data.status === "UNDER_VERIFICATION"
        ? ["VERIFY", ...(data.type === "NEW" ? ["CORRECTION"] : []), "REJECT"]
        : data.status === "INSPECTION_PENDING"
          ? ["SCHEDULE", "PASS", "FAIL", "APPROVE", "REJECT"]
          : ["REJECT"];
  return (
    <section className="card p-4">
      <div className="d-flex justify-content-between gap-3">
        <div>
          <span className="eyebrow">APPLICATION DETAILS</span>
          <h2 className="mt-2">{data.vehicle}</h2>
          <p className="text-secondary small text-break">{data.reference}</p>
        </div>
        <button
          className="btn btn-outline-secondary align-self-start"
          onClick={onClose}
        >
          Close
        </button>
      </div>
      <Status value={data.status} />
      <Progress data={data} />
      <div className="row mt-3 g-3">
        <div className="col-md-6">
          <h3>Owner and vehicle</h3>
          <dl>
            <dt>Applicant</dt>
            <dd>{data.owner}</dd>
            <dt>Category / fuel</dt>
            <dd>
              {label(data.vehicleDetails.vehicleCategory)} /{" "}
              {label(data.vehicleDetails.fuelType)}
            </dd>
            <dt>Chassis / engine</dt>
            <dd className="text-break">
              {data.vehicleDetails.chassisNumber} /{" "}
              {data.vehicleDetails.engineNumber}
            </dd>
            <dt>Manufacture year</dt>
            <dd>{data.vehicleDetails.manufactureYear}</dd>
          </dl>
        </div>
        <div className="col-md-6">
          <h3>Review and payment</h3>
          <p>
            Fee: <strong>{money(data.amount)}</strong> ·{" "}
            {data.paid ? "Payment recorded" : "Awaiting payment"}
          </p>
          <p>
            Inspection: {label(data.inspectionStatus)} ·{" "}
            {data.inspectionDate || "Not scheduled"}
          </p>
          {[
            data.verificationRemark,
            data.inspectionRemark,
            data.decisionRemarks,
          ]
            .filter(Boolean)
            .map((x, i) => (
              <p key={i} className="text-secondary">
                {x}
              </p>
            ))}
        </div>
      </div>
      {data.payment && (
        <div className="border-top py-3">
          <h3>Payment receipt</h3>
          <p className="mb-0 text-break">
            {data.payment.reference} · {money(data.payment.amount)} · Card
            ending {data.payment.lastFour}
          </p>
          <small>
            Simulation recorded at{" "}
            {new Date(data.payment.paidAt).toLocaleString()}
          </small>
        </div>
      )}
      {applicant && !closed && !data.paid && (
        <div className="border-top py-3">
          <h3>Record simulated payment</h3>
          <p className="small text-secondary">
            Enter only the final four card digits. This demo does not charge a
            card.
          </p>
          <Form
            busy={busy}
            button={`Record ${money(data.amount)}`}
            fields={[
              {
                name: "cardLastFourDigit",
                title: "Card last four digits",
                pattern: "[0-9]{4}",
                maxLength: 4,
              },
            ]}
            onSubmit={(x) =>
              act("payments", "POST", { ...x, amount: data.amount })
            }
          />
        </div>
      )}
      {applicant &&
        data.type === "NEW" &&
        data.status === "CORRECTION_REQUIRED" && (
          <div className="border-top py-3">
            <h3>Correct and resubmit vehicle details</h3>
            <VehicleForm
              key={data.reference}
              initial={data.vehicleDetails}
              busy={busy}
              button="Resubmit corrections"
              onSubmit={(x) => act("correction", "PUT", x)}
            />
          </div>
        )}
      {reviewer && !closed && (
        <div className="border-top py-3">
          <h3>Officer review</h3>
          <label className="form-label" htmlFor="remarks">
            Remarks
          </label>
          <textarea
            id="remarks"
            className="form-control mb-3"
            maxLength={500}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
          />
          <label className="form-label" htmlFor="appointment">
            Inspection appointment
          </label>
          <input
            id="appointment"
            type="date"
            className="form-control mb-3"
            value={appointment}
            min={today()}
            onChange={(e) => setAppointment(e.target.value)}
          />
          <div className="d-flex flex-wrap gap-2">
            {actions.map((action) => (
              <button
                key={action}
                disabled={
                  busy ||
                  (["CORRECTION", "PASS", "FAIL", "REJECT"].includes(action) &&
                    !remarks.trim()) ||
                  (["PASS", "FAIL"].includes(action) &&
                    (!data.inspectionDate || data.inspectionDate > today())) ||
                  (action === "APPROVE" &&
                    (!data.paid || data.inspectionStatus !== "PASSED"))
                }
                className={`btn btn-${action === "REJECT" || action === "FAIL" ? "outline-danger" : "outline-primary"}`}
                onClick={() =>
                  act("review", "POST", { action, remarks, appointment })
                }
              >
                {
                  {
                    START: "Start verification",
                    VERIFY: "Verify details",
                    CORRECTION: "Request correction",
                    SCHEDULE: "Schedule inspection",
                    PASS: "Pass inspection",
                    FAIL: "Fail inspection",
                    APPROVE: "Approve & issue RC",
                    REJECT: "Reject application",
                  }[action]
                }
              </button>
            ))}
          </div>
        </div>
      )}
      {data.status === "APPROVED" && (
        <div className="certificate-panel mt-4">
          <div className="d-flex justify-content-between flex-wrap gap-3 align-items-start">
            <div>
              <span className="eyebrow">REGISTRATION CERTIFICATE</span>
              <h3 className="mt-2">Your vehicle is registered</h3>
            </div>
            <button
              className="btn btn-outline-primary"
              disabled={busy}
              onClick={() =>
                run(async () =>
                  setCertificate(
                    await api(
                      `/applications/${encodeURIComponent(data.reference)}/certificate`,
                    ),
                  ),
                )
              }
            >
              View RC details
            </button>
          </div>
          {(certificate || data.certificate) && (
            <div className="row g-3 mt-2">
              <div className="col-md-6">
                <div className="number-plate">
                  {(certificate || data.certificate).registrationNumber}
                </div>
              </div>
              <div className="col-md-6">
                <dl className="mb-0">
                  <dt>Registered owner</dt>
                  <dd>
                    {(certificate || data.certificate).registeredOwnerName}
                  </dd>
                  <dt>Issued on</dt>
                  <dd>{(certificate || data.certificate).issuedDate}</dd>
                  <dt>Valid until</dt>
                  <dd>{(certificate || data.certificate).validTill}</dd>
                </dl>
              </div>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
