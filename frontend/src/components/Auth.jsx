import { useState } from "react";
import Form from "./Form";
import { api, today } from "../api";

export default function Auth({ onLogin, run, busy }) {
  const [signup, setSignup] = useState(false);
  const [staff, setStaff] = useState(false);
  const fields = signup
    ? staff
      ? [
          { name: "firstName", title: "First name", maxLength: 30 },
          { name: "lastName", title: "Last name", maxLength: 30 },
          {
            name: "emailAddress",
            title: "Email address",
            type: "email",
            maxLength: 100,
          },
          {
            name: "phoneNumber",
            title: "Phone number",
            pattern: "[0-9+]{10,15}",
          },
          {
            name: "password",
            title: "Password (10–128 characters)",
            type: "password",
            minLength: 10,
            maxLength: 128,
          },
          {
            name: "designation",
            options: ["CLERK", "OFFICER", "SENIOR_OFFICER", "ADMIN"],
          },
          {
            name: "role",
            options: ["RTO_OFFICER", "RTO_ADMIN", "SYSTEM_ADMIN"],
          },
        ]
      : [
          {
            name: "firstName",
            title: "First name",
            maxLength: 30,
            autoComplete: "given-name",
          },
          {
            name: "lastName",
            title: "Last name",
            maxLength: 30,
            autoComplete: "family-name",
          },
          {
            name: "emailAddress",
            title: "Email address",
            type: "email",
            maxLength: 100,
            autoComplete: "email",
          },
          {
            name: "phoneNumber",
            title: "Phone number",
            pattern: "[0-9+]{10,15}",
            maxLength: 15,
            autoComplete: "tel",
          },
          {
            name: "password",
            title: "Password (10–128 characters)",
            type: "password",
            minLength: 10,
            maxLength: 128,
            autoComplete: "new-password",
          },
          {
            name: "dateOfBirth",
            title: "Date of birth",
            type: "date",
            max: today(),
            autoComplete: "bday",
          },
          {
            name: "identityProofType",
            title: "Identity proof type",
            options: [
              "AADHAAR",
              "PAN",
              "PASSPORT",
              "DRIVING_LICENSE",
              "VOTER_ID",
            ],
          },
          {
            name: "identityProofNumber",
            title: "Identity proof number",
            maxLength: 30,
          },
          {
            name: "address",
            title: "Residential address",
            maxLength: 150,
            autoComplete: "street-address",
          },
          {
            name: "cityName",
            title: "City",
            maxLength: 50,
            autoComplete: "address-level2",
          },
          {
            name: "stateName",
            title: "State",
            maxLength: 50,
            autoComplete: "address-level1",
          },
          {
            name: "pincode",
            title: "PIN code",
            pattern: "[0-9]{6}",
            maxLength: 6,
            autoComplete: "postal-code",
          },
        ]
    : [
        {
          name: "email",
          title: "Email address",
          type: "email",
          maxLength: 100,
          autoComplete: "username",
        },
        {
          name: "password",
          title: "Password",
          type: "password",
          maxLength: 128,
          autoComplete: "current-password",
        },
      ];
  return (
    <main className={`auth-wrap ${signup ? "signup-layout" : ""}`}>
      <section className="card auth-card">
        <div className="eyebrow mb-3">
          {signup
            ? staff
              ? "EMPLOYEE REGISTRATION"
              : "OWNER REGISTRATION"
            : "YOUR REGISTRATION WORKSPACE"}
        </div>
        <h2>
          {signup
            ? staff
              ? "Create an employee account"
              : "Create your owner account"
            : "Welcome back"}
        </h2>
        <p className="text-secondary mb-4">
          {signup
            ? "Your identity and contact details will be linked to your applications."
            : "Sign in to apply, track progress, or review applications."}
        </p>
        {!signup && (
          <div
            className="role-tabs mb-4"
            role="group"
            aria-label="Account type"
          >
            <button
              type="button"
              disabled={busy}
              aria-pressed={!staff}
              className={!staff ? "selected" : ""}
              onClick={() => setStaff(false)}
            >
              Vehicle owner
            </button>
            <button
              type="button"
              disabled={busy}
              aria-pressed={staff}
              className={staff ? "selected" : ""}
              onClick={() => setStaff(true)}
            >
              RTO employee
            </button>
          </div>
        )}
        <Form
          key={`${signup}-${staff}`}
          fields={fields}
          busy={busy}
          button={signup ? "Create account & continue" : "Sign in to workspace"}
          onSubmit={(values) =>
            run(async () => {
              if (signup && staff) {
                await api("/employees", "POST", values);
                onLogin(
                  await api("/auth/login", "POST", {
                    email: values.emailAddress,
                    password: values.password,
                    staff: true,
                  }),
                );
              } else {
                onLogin(
                  await api(
                    signup ? "/auth/signup" : "/auth/login",
                    "POST",
                    signup ? values : { ...values, staff },
                  ),
                );
              }
            })
          }
        />
        <div className="auth-switch">
          <span>
            {signup ? "Already have an account?" : "Need an account?"}
          </span>
          <button
            disabled={busy}
            className="btn btn-link p-0"
            onClick={() => setSignup(!signup)}
          >
            {signup
              ? "Sign in"
              : staff
                ? "Create an employee account"
                : "Create an owner account"}
          </button>
        </div>
        
      </section>
    </main>
  );
}
