import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { LogoMark } from "./LogoMark";

const points = [
  { id: "bill", title: "Faturalar", text: "Elektrik, su ve doğalgaz" },
  { id: "receipt", title: "Fişler", text: "Alışveriş ve iade süresi" },
  { id: "warranty", title: "Garantiler", text: "Kapsamın bitiş tarihi" },
] as const;

function PointIcon({ name }: { name: "bill" | "receipt" | "warranty" }) {
  return (
    <svg className={`point-icon point-${name}`} viewBox="0 0 24 24" aria-hidden="true">
      {name === "bill" && (
        <>
          <path d="M7 4.75h6.1L17.25 8.9V18.6c0 .8-.65 1.45-1.45 1.45H7A1.45 1.45 0 0 1 5.55 18.6V6.2c0-.8.65-1.45 1.45-1.45z" />
          <path d="M13.1 4.75V9h4.15" />
          <path d="M8.4 12.7h6.3M8.4 15.6h4.1" />
        </>
      )}
      {name === "receipt" && (
        <>
          <path d="M6.4 6.2h11.2v3.05a1.55 1.55 0 0 0 0 3.1v3.05H6.4v-3.05a1.55 1.55 0 0 0 0-3.1V6.2z" />
          <path d="M9 9.15h6M9 12h6M9 14.85h3.6" />
        </>
      )}
      {name === "warranty" && (
        <>
          <path d="M12 4.35 18.15 6.7v4.85c0 3.15-2.25 5.35-6.15 6.9-3.9-1.55-6.15-3.75-6.15-6.9V6.7L12 4.35z" />
          <path d="M9.35 11.85 11.15 13.65 14.7 10.1" />
        </>
      )}
    </svg>
  );
}

export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="auth-screen">
      <section className="auth-panel">
        <div className="auth-brand">
          <Link to="/giris" className="auth-mark" aria-label="Bill Box giriş">
            <LogoMark />
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
                <PointIcon name={point.id} />
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
