import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Message } from "primereact/message";
import { useAuth } from "../auth/AuthContext";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Giriş başarısız.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-screen">
      <form className="auth-card" onSubmit={submit}>
        <h2>Bill Box</h2>
        <p className="page-subtitle">Faturalarınızın profesyonel kasası</p>
        {error && <Message severity="error" text={error} className="w-full mb-3" />}
        <label className="block mb-2 mt-3">E-posta</label>
        <InputText value={email} onChange={(e) => setEmail(e.target.value)} className="w-full" required />
        <label className="block mb-2 mt-3">Şifre</label>
        <Password value={password} onChange={(e) => setPassword(e.target.value)} feedback={false} toggleMask className="w-full" inputClassName="w-full" />
        <Button type="submit" label="Giriş yap" className="w-full mt-4" loading={loading} />
        <p className="mt-3">
          Hesabınız yok mu? <Link to="/kayit">Kayıt olun</Link>
        </p>
      </form>
    </div>
  );
}
