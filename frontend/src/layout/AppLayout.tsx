import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { Badge } from "primereact/badge";
import { Button } from "primereact/button";
import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { LogoMark } from "../components/LogoMark";

const groups = [
  {
    label: "Kasa",
    links: [
      { to: "/", label: "Özet", icon: "pi pi-th-large" },
      { to: "/kasa", label: "Tüm belgeler", icon: "pi pi-inbox" },
    ],
  },
  {
    label: "Belgeler",
    links: [
      { to: "/faturalar", label: "Faturalar", icon: "pi pi-file" },
      { to: "/fisler", label: "Fişler", icon: "pi pi-ticket" },
      { to: "/garantiler", label: "Garantiler", icon: "pi pi-shield" },
    ],
  },
  {
    label: "Takip",
    links: [
      { to: "/tekrarlayan", label: "Tekrarlayan", icon: "pi pi-replay" },
      { to: "/butceler", label: "Bütçeler", icon: "pi pi-wallet" },
      { to: "/raporlar", label: "Raporlar", icon: "pi pi-chart-bar" },
      { to: "/uyarilar", label: "Uyarılar", icon: "pi pi-bell" },
    ],
  },
  {
    label: "Yönetim",
    links: [
      { to: "/cariler", label: "Cariler", icon: "pi pi-users" },
      { to: "/kategoriler", label: "Kategoriler", icon: "pi pi-tags" },
      { to: "/ayarlar", label: "Ayarlar", icon: "pi pi-cog" },
    ],
  },
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
          <LogoMark className="brand-mark" />
          <div>
            <h1>Bill Box</h1>
            <p>{user?.organizationName}</p>
          </div>
        </div>
        <nav>
          {groups.map((group) => (
            <div key={group.label} className="nav-group">
              <div className="nav-label">{group.label}</div>
              {group.links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === "/"}
                  className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
                >
                  <i className={link.icon} />
                  <span>{link.label}</span>
                  {link.to === "/uyarilar" && unread > 0 && <Badge value={unread} severity="danger" />}
                </NavLink>
              ))}
            </div>
          ))}
        </nav>
        <div className="sidebar-foot">
          <strong>{user?.fullName}</strong>
          <span>{user?.email}</span>
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div className="topbar-title">Ev kasası</div>
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
