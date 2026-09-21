import type { ReactNode } from "react";
import { Link } from "react-router-dom";

const points = [
  { icon: "pi pi-file", title: "Faturalar", text: "Elektrik, su ve doğalgaz" },
  { icon: "pi pi-ticket", title: "Fişler", text: "Alışveriş ve iade süresi" },
  { icon: "pi pi-shield", title: "Garantiler", text: "Kapsamın bitiş tarihi" },
];

export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="auth-screen">
      <section className="auth-panel">
        <div className="auth-brand">
          <Link to="/giris" className="auth-mark" aria-label="Bill Box giriş">
            B
          </Link>
          <div>
            <strong>Bill Box</strong>
            <span>Ev kasası</span>
          </div>
        </div>

        <div className="auth-copy">
          <p className="auth-kicker">Belgeler</p>
          <h1>
            Faturalar, fişler
            <br />
            ve garantiler
            <br />
            tek kasada.
          </h1>
          <p className="auth-lead">Ödeme ve bitiş tarihleri tek bakışta görünür.</p>
          <ul className="auth-points">
            {points.map((point) => (
              <li key={point.title}>
                <i className={point.icon} aria-hidden="true" />
                <div>
                  <strong>{point.title}</strong>
                  <span>{point.text}</span>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className="auth-stage" aria-hidden="true">
          <article className="doc-card doc-bill">
            <header>
              <span>Fatura</span>
              <em>Vadesi geçti</em>
            </header>
            <strong>AYEDAŞ</strong>
            <p>Elektrik · Mart</p>
            <footer>
              <span>FAT-2026-001</span>
              <b>₺2.940</b>
            </footer>
          </article>
          <article className="doc-card doc-receipt">
            <header>
              <span>Fiş</span>
              <em>İade açık</em>
            </header>
            <strong>MediaMarkt</strong>
            <p>Buzdolabı</p>
            <footer>
              <span>01.08.2026</span>
              <b>₺28.500</b>
            </footer>
          </article>
          <article className="doc-card doc-warranty">
            <header>
              <span>Garanti</span>
              <em>24 gün</em>
            </header>
            <strong>Bosch</strong>
            <p>15.10.2026’ya kadar</p>
          </article>
        </div>
      </section>

      <section className="auth-main">{children}</section>
    </main>
  );
}
