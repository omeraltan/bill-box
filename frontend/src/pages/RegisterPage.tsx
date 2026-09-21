import { FormEvent, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { AuthShell } from "../components/AuthShell";
import { useAuth } from "../auth/AuthContext";
import { useCapsLock } from "../auth/useCapsLock";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [organizationName, setOrganizationName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { capsLock, syncCapsLock, clearCapsLock } = useCapsLock();

  if (user) {
    return <Navigate to="/" replace />;
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const nextEmail = email.trim();
    if (!fullName.trim() || !emailPattern.test(nextEmail) || password.length < 8) {
      setError("Ad, geçerli e-posta ve en az 8 karakterlik şifre gerekli.");
      return;
    }

    setLoading(true);
    setError("");
    try {
      await register({
        fullName: fullName.trim(),
        email: nextEmail,
        password,
        organizationName: organizationName.trim() || undefined,
      });
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Kayıt başarısız.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <form className="auth-form" onSubmit={submit} noValidate>
        <header>
          <p className="kicker">Yeni kasa</p>
          <h2>Kendi kasanızı açın</h2>
          <p>Kişisel ev kasanız hazır. Ekip daveti daha sonra eklenecek.</p>
        </header>

        {error && (
          <div className="auth-alert" role="alert">
            <i className="pi pi-exclamation-circle" aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        <label className="auth-field" htmlFor="fullName">
          <span>Ad soyad</span>
          <span className="auth-input">
            <i className="pi pi-user" aria-hidden="true" />
            <InputText
              id="fullName"
              autoComplete="name"
              value={fullName}
              onChange={(event) => setFullName(event.target.value)}
              required
            />
          </span>
        </label>

        <label className="auth-field" htmlFor="register-email">
          <span>E-posta</span>
          <span className="auth-input">
            <i className="pi pi-envelope" aria-hidden="true" />
            <InputText
              id="register-email"
              type="email"
              inputMode="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </span>
        </label>

        <label className="auth-field" htmlFor="organizationName">
          <span>Kasa adı</span>
          <span className="auth-input">
            <i className="pi pi-home" aria-hidden="true" />
            <InputText
              id="organizationName"
              value={organizationName}
              onChange={(event) => setOrganizationName(event.target.value)}
              placeholder="Örneğin Altan Kasası"
            />
          </span>
        </label>

        <label className="auth-field" htmlFor="register-password">
          <span>Şifre</span>
          <span className="auth-input">
            <i className="pi pi-lock" aria-hidden="true" />
            <Password
              inputId="register-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onKeyDown={syncCapsLock}
              onKeyUp={syncCapsLock}
              onMouseDown={syncCapsLock}
              onBlur={clearCapsLock}
              toggleMask
              autoComplete="new-password"
              promptLabel="Şifre gücü"
              weakLabel="Zayıf"
              mediumLabel="Orta"
              strongLabel="Güçlü"
              aria-describedby={capsLock ? "register-caps-lock" : undefined}
              pt={{
                showIcon: { "aria-label": "Şifreyi göster" },
                hideIcon: { "aria-label": "Şifreyi gizle" },
              }}
            />
          </span>
          <small>En az 8 karakter.</small>
          {capsLock && (
            <small id="register-caps-lock" className="auth-caps" role="status">
              Caps Lock açık.
            </small>
          )}
        </label>

        <Button
          type="submit"
          label="Kasayı oluştur"
          icon="pi pi-arrow-right"
          iconPos="right"
          className="auth-submit"
          loading={loading}
        />

        <p className="auth-switch">
          Zaten hesabınız var mı? <Link to="/giris">Giriş yapın</Link>
        </p>
      </form>
    </AuthShell>
  );
}
