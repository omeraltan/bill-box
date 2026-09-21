import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { Badge } from "primereact/badge";
import { Button } from "primereact/button";
import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";

const links = [
  { to: "/", label: "Özet", icon: "pi pi-th-large" },
  { to: "/faturalar", label: "Faturalar", icon: "pi pi-file" },
  { to: "/faturalar/yeni", label: "Yeni fatura", icon: "pi pi-plus" },
  { to: "/cariler", label: "Cariler", icon: "pi pi-users" },
  { to: "/kategoriler", label: "Kategoriler", icon: "pi pi-tags" },
  { to: "/tekrarlayan", label: "Tekrarlayan", icon: "pi pi-replay" },
  { to: "/butceler", label: "Bütçeler", icon: "pi pi-wallet" },
  { to: "/raporlar", label: "Raporlar", icon: "pi pi-chart-bar" },
  { to: "/uyarilar", label: "Uyarılar", icon: "pi pi-bell" },
  { to: "/ayarlar", label: "Ayarlar", icon: "pi pi-cog" },
];

export function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    api<{ count: number }>("/api/alerts/unread-count")
      .then((data) => setUnread(data.count))
      .catch(() => undefined);
  }, []);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">B</div>
          <div>
            <h1>Bill Box</h1>
            <p>{user?.organizationName}</p>
          </div>
        </div>
        <nav>
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.to === "/"} className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
              <i className={link.icon} />
              <span>{link.label}</span>
              {link.to === "/uyarilar" && unread > 0 && <Badge value={unread} severity="danger" />}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div>
        <header className="topbar">
          <div>
            <div className="page-subtitle">Fatura kasası</div>
            <strong>{user?.fullName}</strong>
          </div>
          <Button
            label="Çıkış"
            icon="pi pi-sign-out"
            text
            onClick={() => {
              logout();
              navigate("/giris");
            }}
          />
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
