import Form from "./Form";
import { categories } from "../api";
export const vehicleFields = [
  { name: "vehicleCategory", options: categories },
  { name: "manufacturerName", maxLength: 50 },
  { name: "modelName", maxLength: 50 },
  { name: "chassisNumber", maxLength: 50 },
  { name: "engineNumber", maxLength: 50 },
  {
    name: "fuelType",
    options: ["PETROL", "DIESEL", "ELECTRIC", "CNG", "HYBRID"],
  },
  {
    name: "manufactureYear",
    type: "number",
    min: 1900,
    max: new Date().getFullYear(),
  },
  { name: "colorVariant", optional: true, maxLength: 30 },
];
export default function VehicleForm({
  onSubmit,
  initial,
  busy,
  button = "Submit application",
}) {
  return (
    <Form
      fields={vehicleFields.map((field) => ({
        ...field,
        disabled: field.name === "vehicleCategory" && Boolean(initial),
      }))}
      initial={initial}
      busy={busy}
      button={button}
      onSubmit={(x) =>
        onSubmit({
          ...Object.fromEntries(
            vehicleFields.map(({ name }) => [name, x[name] ?? ""]),
          ),
          manufactureYear: Number(x.manufactureYear),
        })
      }
    />
  );
}
