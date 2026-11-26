import { useEffect, useRef } from "react";

// A table checkbox. For the header "select all" box, `indeterminate` shows a dash
// when only some rows are ticked.
export default function SelectionCheckbox({ checked, indeterminate = false, onChange, label, disabled }) {
  const box = useRef(null);
  useEffect(() => {
    if (box.current) box.current.indeterminate = indeterminate;
  }, [indeterminate]);
  return (
    <input
      ref={box}
      type="checkbox"
      className="form-check-input"
      checked={checked}
      disabled={disabled}
      onChange={onChange}
      aria-label={label}
    />
  );
}
