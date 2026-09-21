import { useState, type KeyboardEvent, type MouseEvent } from "react";

export function useCapsLock() {
  const [capsLock, setCapsLock] = useState(false);

  const syncCapsLock = (event: KeyboardEvent | MouseEvent) => {
    setCapsLock(event.getModifierState("CapsLock"));
  };

  const clearCapsLock = () => setCapsLock(false);

  return { capsLock, syncCapsLock, clearCapsLock };
}
