import { api } from "../api";
import VehicleForm from "./VehicleForm";
import FeeSchedule from "./FeeSchedule";

export default function NewRegistrationPage({ ownerId, run, busy, onChange }) {
  return (
    <section className="card p-4">
      <h2>Vehicle information</h2>
      <p className="text-secondary">
        The application will be linked to your owner record.
      </p>
      <VehicleForm
        busy={busy}
        onSubmit={(values) =>
          run(async () =>
            onChange(
              await api(`/applications?ownerId=${ownerId}`, "POST", values),
            ),
          )
        }
      />
      <div className="mt-4">
        <FeeSchedule />
      </div>
    </section>
  );
}
