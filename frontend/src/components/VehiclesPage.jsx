import { label } from "../api";

export default function VehiclesPage({ vehicles }) {
  return (
    <div className="row g-3">
      {vehicles.map((vehicle) => (
        <div className="col-xl-6" key={vehicle.temporaryregisterNo}>
          <section className="card p-4">
            <h2>
              {vehicle.manufacturerName} {vehicle.modelName}
            </h2>
            <p>{label(vehicle.vehicleCategory)}</p>
            <p>
              {vehicle.registrationcertificateNumber || "Registration pending"}
            </p>
            <p>Valid until: {vehicle.registrationValidTill || "Not issued"}</p>
          </section>
        </div>
      ))}
      {vehicles.length === 0 && <p>No vehicle records yet.</p>}
    </div>
  );
}
