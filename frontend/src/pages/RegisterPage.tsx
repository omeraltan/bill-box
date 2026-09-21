import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Message } from "primereact/message";
import { useAuth } from "../auth/AuthContext";

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [organizationName, setOrganizationName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await register({ fullName, email, password, organizationName });
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Kayıt başarısız.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-screen">
      <form className="auth-card" onSubmit={submit}>
        <h2>Kasa oluştur</h2>
        <p className="page-subtitle">Kişisel kasanız ev ve ekip için hazır.</p>
        {error && <Message severity="error" text={error} className="w-full mb-3" />}
        <label className="block mb-2 mt-3">Ad soyad</label>
        <InputText value={fullName} onChange={(e) => setFullName(e.target.value)} className="w-full" required />
        <label className="block mb-2 mt-3">E-posta</label>
        <InputText value={email} onChange={(e) => setEmail(e.target.value)} className="w-full" required />
        <label className="block mb-2 mt-3">Organizasyon adı</label>
        <InputText value={organizationName} onChange={(e) => setOrganizationName(e.target.value)} className="w-full" placeholder="Opsiyonel" />
        <label className="block mb-2 mt-3">Şifre</label>
        <Password value={password} onChange={(e) => setPassword(e.target.value)} toggleMask className="w-full" inputClassName="w-full" />
        <Button type="submit" label="Kaydı tamamla" className="w-full mt-4" loading={loading} />
        <p className="mt-3">
          Zaten hesabınız var mı? <Link to="/giris">Giriş yapın</Link>
        </p>
      </form>
    </div>
  );
}
