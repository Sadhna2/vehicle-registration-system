import { useState } from "react";
import Form from "./Form";
import { api, today } from "../api";



export default function Auth({ onLogin, run, busy }) {
  const [signup, setSignup] = useState(false);
  const [staff, setStaff] = useState(false);
  const fields = signup
    ? [
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
      <div className="auth-intro">
        <div className="eyebrow intro-eyebrow">
          <span className="live-dot" /> NEW VEHICLE REGISTRATION
        </div>
        <h1>
          A new vehicle.
          <br />A clear road ahead.
        </h1>
        <p className="intro-description">
          From your first application to your registration certificate. Follow
          every step in one place.
        </p>
        <VehicleIllustration />
        <div className="intro-steps">
          {[
            ["01", "Apply", "Add your vehicle details"],
            ["02", "Track", "Follow verification & inspection"],
            ["03", "Get your RC", "View your issued certificate"],
          ].map(([number, title, description]) => (
            <div key={number}>
              <span>{number}</span>
              <strong>{title}</strong>
              <small>{description}</small>
            </div>
          ))}
        </div>
        <p className="intro-footnote">
          Vehicle owner services · RTO review · Registration certificates
        </p>
      </div>
      <section className="card auth-card">
        <div className="eyebrow mb-3">
          {signup ? "OWNER REGISTRATION" : "YOUR REGISTRATION WORKSPACE"}
        </div>
        <h2>{signup ? "Create your owner account" : "Welcome back"}</h2>
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
            run(async () =>
              onLogin(
                await api(
                  signup ? "/auth/signup" : "/auth/login",
                  "POST",
                  signup ? values : { ...values, staff },
                ),
              ),
            )
          }
        />
        <div className="auth-switch">
          <span>
            {signup
              ? "Already have an account?"
              : "Registering your first vehicle?"}
          </span>
          <button
            disabled={busy}
            className="btn btn-link p-0"
            onClick={() => setSignup(!signup)}
          >
            {signup ? "Sign in" : "Create an owner account"}
          </button>
        </div>
        <p className="auth-help">
          {staff && !signup
            ? "Use the employee account provided by your RTO administrator."
            : "Keep your identity details, chassis number, and engine number ready."}
        </p>
      </section>
    </main>
  );
}
