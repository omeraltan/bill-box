import { useEffect, useLayoutEffect, useRef, useState, type PointerEvent, type ReactNode } from "react";

type Field = "email" | "password";
type Layer = "letters" | "symbols";

type VirtualKeyboardProps = {
  email: string;
  password: string;
  disabled?: boolean;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
};

const LETTERS = [
  ["q", "w", "e", "r", "t", "y", "u", "ı", "o", "p", "ğ", "ü"],
  ["a", "s", "d", "f", "g", "h", "j", "k", "l", "ş", "i"],
  ["z", "x", "c", "v", "b", "n", "m", "ö", "ç"],
];

const SYMBOLS = [
  ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"],
  ["@", "#", "₺", "&", "*", "-", "+", "(", ")", "/"],
  ["!", "?", "'", "\"", "_", "=", ".", ",", "%", "\\"],
];

function shown(char: string, shift: boolean) {
  return shift ? char.toLocaleUpperCase("tr") : char;
}

export function VirtualKeyboard({
  email,
  password,
  disabled = false,
  onEmailChange,
  onPasswordChange,
}: VirtualKeyboardProps) {
  const [open, setOpen] = useState(false);
  const [layer, setLayer] = useState<Layer>("letters");
  const [shift, setShift] = useState(false);
  const [active, setActive] = useState<Field>("email");
  const emailRef = useRef(email);
  const passwordRef = useRef(password);
  const activeRef = useRef(active);
  const onEmailRef = useRef(onEmailChange);
  const onPasswordRef = useRef(onPasswordChange);
  const caretRef = useRef<number | null>(null);
  const selectionRef = useRef<{ start: number; end: number } | null>(null);
  const repeatRef = useRef<{ timeout: number; interval: number } | null>(null);

  emailRef.current = email;
  passwordRef.current = password;
  activeRef.current = active;
  onEmailRef.current = onEmailChange;
  onPasswordRef.current = onPasswordChange;

  useEffect(() => {
    const onFocus = (event: FocusEvent) => {
      const id = (event.target as HTMLElement | null)?.id;
      if (id === "email" || id === "password") {
        activeRef.current = id;
        setActive(id);
      }
    };
    const clearSelection = (event: Event) => {
      const id = (event.target as HTMLElement | null)?.id;
      if (id === "email" || id === "password") {
        selectionRef.current = null;
      }
    };
    document.addEventListener("focusin", onFocus);
    document.addEventListener("keyup", clearSelection);
    document.addEventListener("pointerup", clearSelection);
    return () => {
      document.removeEventListener("focusin", onFocus);
      document.removeEventListener("keyup", clearSelection);
      document.removeEventListener("pointerup", clearSelection);
      stopRepeat(repeatRef);
    };
  }, []);

  useLayoutEffect(() => {
    if (caretRef.current == null) {
      return;
    }
    const input = document.getElementById(active) as HTMLInputElement | null;
    const caret = caretRef.current;
    caretRef.current = null;
    if (!input) {
      return;
    }
    if (document.activeElement !== input) {
      input.focus();
    }
    try {
      input.setSelectionRange(caret, caret);
    } catch {
      selectionRef.current = null;
    }
  }, [email, password, active]);

  const focusField = (field: Field) => {
    activeRef.current = field;
    setActive(field);
    document.getElementById(field)?.focus();
  };

  const openKeyboard = () => {
    const focused = document.activeElement?.id;
    const next: Field =
      focused === "email" || focused === "password" ? focused : email.trim() === "" ? "email" : "password";
    setLayer("letters");
    setShift(false);
    activeRef.current = next;
    setActive(next);
    setOpen(true);
    requestAnimationFrame(() => document.getElementById(next)?.focus());
  };

  const edit = (mutate: (value: string, start: number, end: number) => { value: string; caret: number }) => {
    const field = activeRef.current;
    const input = document.getElementById(field) as HTMLInputElement | null;
    const fallback = field === "email" ? emailRef.current : passwordRef.current;
    const current = input?.value ?? fallback;
    const start = selectionRef.current?.start ?? input?.selectionStart ?? current.length;
    const end = selectionRef.current?.end ?? input?.selectionEnd ?? current.length;
    const result = mutate(current, start, end);
    selectionRef.current = { start: result.caret, end: result.caret };
    caretRef.current = result.caret;
    if (field === "email") {
      emailRef.current = result.value;
      onEmailRef.current(result.value);
    } else {
      passwordRef.current = result.value;
      onPasswordRef.current(result.value);
    }
  };

  const typeChar = (char: string) => {
    if (disabled) {
      return;
    }
    edit((value, start, end) => ({
      value: value.slice(0, start) + char + value.slice(end),
      caret: start + char.length,
    }));
  };

  const backspace = () => {
    if (disabled) {
      return;
    }
    edit((value, start, end) => {
      if (start !== end) {
        return { value: value.slice(0, start) + value.slice(end), caret: start };
      }
      if (start === 0) {
        return { value, caret: 0 };
      }
      return { value: value.slice(0, start - 1) + value.slice(end), caret: start - 1 };
    });
  };

  const pressLetter = (char: string) => {
    typeChar(shift ? char.toLocaleUpperCase("tr") : char);
    if (shift) {
      setShift(false);
    }
  };

  const clearField = () => {
    if (disabled) {
      return;
    }
    edit(() => ({ value: "", caret: 0 }));
  };

  const holdBackspace = (event: PointerEvent<HTMLButtonElement>) => {
    event.preventDefault();
    stopRepeat(repeatRef);
    backspace();
    const timeout = window.setTimeout(() => {
      const interval = window.setInterval(backspace, 55);
      if (repeatRef.current) {
        repeatRef.current.interval = interval;
      }
    }, 380);
    repeatRef.current = { timeout, interval: 0 };
  };

  const keepFocus = (event: PointerEvent<HTMLButtonElement>) => {
    event.preventDefault();
  };

  if (!open) {
    return (
      <button type="button" className="vkbd-toggle" onClick={openKeyboard} disabled={disabled}>
        <KeyboardGlyph />
        Sanal klavye
      </button>
    );
  }

  const letters = layer === "letters";

  return (
    <div
      className="vkbd"
      role="group"
      aria-label="Sanal klavye"
      onMouseDown={(event) => {
        if ((event.target as HTMLElement).closest("[data-vkbd-focus]")) {
          return;
        }
        event.preventDefault();
      }}
    >
      <div className="vkbd-head">
        <span>Sanal klavye</span>
        <div className="vkbd-target" role="group" aria-label="Yazılacak alan">
          <button type="button" data-vkbd-focus="" aria-pressed={active === "email"} onClick={() => focusField("email")}>
            E-posta
          </button>
          <button type="button" data-vkbd-focus="" aria-pressed={active === "password"} onClick={() => focusField("password")}>
            Şifre
          </button>
        </div>
        <button
          type="button"
          className="vkbd-close"
          data-vkbd-focus=""
          aria-label="Sanal klavyeyi kapat"
          onClick={() => setOpen(false)}
        >
          <i className="pi pi-times" aria-hidden="true" />
        </button>
      </div>

      {(letters ? LETTERS : SYMBOLS).map((row, index) => (
        <div key={`${letters ? "letters" : "symbols"}-${index}`} className={rowClass(letters, index, row.length)}>
          {letters && index === 2 && (
            <Key
              label="⇧"
              ariaLabel="Büyük harf"
              pressed={shift}
              toggle
              className="vkbd-key vkbd-fn"
              onPointerDown={(event) => {
                keepFocus(event);
                setShift((value) => !value);
              }}
            />
          )}
          {row.map((char) => (
            <Key
              key={char}
              label={letters ? shown(char, shift) : char}
              onPointerDown={(event) => {
                keepFocus(event);
                if (letters) {
                  pressLetter(char);
                } else {
                  typeChar(char);
                }
              }}
            />
          ))}
          {letters && index === 2 && (
            <Key
              label="⌫"
              ariaLabel="Sil"
              className="vkbd-key vkbd-fn"
              onPointerDown={holdBackspace}
              onPointerUp={() => stopRepeat(repeatRef)}
              onPointerCancel={() => stopRepeat(repeatRef)}
              onPointerLeave={() => stopRepeat(repeatRef)}
            />
          )}
        </div>
      ))}

      <div className={letters ? "vkbd-row vkbd-util" : "vkbd-row vkbd-symbol-util"}>
        <Key
          label={letters ? "123" : "ABC"}
          ariaLabel={letters ? "Rakam ve işaretler" : "Harfler"}
          className="vkbd-key vkbd-fn"
          onPointerDown={(event) => {
            keepFocus(event);
            setLayer(letters ? "symbols" : "letters");
            setShift(false);
          }}
        />
        <Key
          label="Temizle"
          ariaLabel="Seçili alanı temizle"
          className="vkbd-key vkbd-fn vkbd-clear"
          onPointerDown={(event) => {
            keepFocus(event);
            clearField();
          }}
        />
        <Key
          label=""
          ariaLabel="Boşluk"
          className="vkbd-key vkbd-space"
          onPointerDown={(event) => {
            keepFocus(event);
            typeChar(" ");
          }}
        />
        {letters ? (
          <>
            <Key
              label="@"
              onPointerDown={(event) => {
                keepFocus(event);
                typeChar("@");
              }}
            />
            <Key
              label="."
              onPointerDown={(event) => {
                keepFocus(event);
                typeChar(".");
              }}
            />
          </>
        ) : (
          <Key
            label="⌫"
            ariaLabel="Sil"
            className="vkbd-key vkbd-fn"
            onPointerDown={holdBackspace}
            onPointerUp={() => stopRepeat(repeatRef)}
            onPointerCancel={() => stopRepeat(repeatRef)}
            onPointerLeave={() => stopRepeat(repeatRef)}
          />
        )}
      </div>
    </div>
  );
}

function stopRepeat(repeatRef: { current: { timeout: number; interval: number } | null }) {
  const repeat = repeatRef.current;
  if (!repeat) {
    return;
  }
  window.clearTimeout(repeat.timeout);
  window.clearInterval(repeat.interval);
  repeatRef.current = null;
}

function rowClass(letters: boolean, index: number, length: number) {
  if (!letters) {
    return "vkbd-row vkbd-row-10";
  }
  if (index === 1) {
    return "vkbd-row vkbd-row-home";
  }
  if (length === 9) {
    return "vkbd-row vkbd-row-shift";
  }
  return "vkbd-row vkbd-row-12";
}

type KeyProps = {
  label: ReactNode;
  ariaLabel?: string;
  className?: string;
  pressed?: boolean;
  toggle?: boolean;
  onClick?: () => void;
  onPointerDown: (event: PointerEvent<HTMLButtonElement>) => void;
  onPointerUp?: () => void;
  onPointerCancel?: () => void;
  onPointerLeave?: () => void;
};

function Key({
  label,
  ariaLabel,
  className = "vkbd-key",
  pressed = false,
  toggle = false,
  onClick,
  onPointerDown,
  onPointerUp,
  onPointerCancel,
  onPointerLeave,
}: KeyProps) {
  return (
    <button
      type="button"
      className={pressed ? `${className} is-on` : className}
      aria-label={ariaLabel}
      aria-pressed={toggle ? pressed : undefined}
      tabIndex={-1}
      onPointerDown={onPointerDown}
      onPointerUp={onPointerUp}
      onPointerCancel={onPointerCancel}
      onPointerLeave={onPointerLeave}
      onClick={onClick}
    >
      {label}
    </button>
  );
}

function KeyboardGlyph() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
      <rect x="2.5" y="5.5" width="19" height="13" rx="2.2" fill="none" stroke="currentColor" strokeWidth="1.6" />
      <path
        d="M7 9.5h.01M11 9.5h.01M15 9.5h.01M19 9.5h.01M7 13h10"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </svg>
  );
}
