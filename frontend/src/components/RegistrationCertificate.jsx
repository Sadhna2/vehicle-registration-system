import { label } from "../api";

function Details({ title, fields }) {
  return (
    <section className="col-lg-6">
      <h4 className="h6 border-bottom pb-2">{title}</h4>
      <dl className="mb-0">
        {fields
          .filter(
            ([, value]) =>
              value !== null && value !== undefined && value !== "",
          )
          .map(([name, value]) => (
            <div className="row mb-2" key={name}>
              <dt className="col-sm-5 small text-secondary">{name}</dt>
              <dd className="col-sm-7 mb-0 text-break">{value}</dd>
            </div>
          ))}
      </dl>
    </section>
  );
}

export default function RegistrationCertificate({ certificate, application }) {
  const vehicle = application.vehicleDetails;
  const owner = vehicle.currentOwner || application.applicant;
  return (
    <article className="mt-3" aria-label="Certificate of registration">
      <h3 className="h4">Certificate of Registration</h3>
      <div className="row g-4 mt-1">
        <Details
          title="Owner and registration"
          fields={[
            ["Registration number", certificate.registrationNumber],
            ["Registered owner", certificate.registeredOwnerName],
            [
              "Address",
              [owner.address, owner.cityName, owner.stateName, owner.pincode]
                .filter(Boolean)
                .join(", ") || null,
            ],
            ["Date of registration", vehicle.firstRegistrationDate],
            ["Registration valid up to", certificate.validTill],
            ["Date of issue", certificate.issuedDate],
            ["Issuing RTO employee", certificate.issuedByEmployeeName],
            ["Application reference", certificate.applicationRefNo],
          ]}
        />
        <Details
          title="Vehicle particulars"
          fields={[
            ["Class of vehicle", label(vehicle.vehicleCategory)],
            ["Manufacturer", vehicle.manufacturerName],
            ["Model", vehicle.modelName],
            ["Year of manufacture", vehicle.manufactureYear],
            ["Chassis number", vehicle.chassisNumber],
            ["Engine number", vehicle.engineNumber],
            ["Fuel used", label(vehicle.fuelType)],
            ["Colour", vehicle.colorVariant],
          ]}
        />
      </div>
    </article>
  );
}
