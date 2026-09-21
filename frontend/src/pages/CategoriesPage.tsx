import { FormEvent, useEffect, useState } from "react";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { Tag } from "primereact/tag";
import { api } from "../api/client";
import type { Category, CategoryKind } from "../api/types";

const empty = { name: "", kind: "EXPENSE" as CategoryKind, color: "#0f766e", icon: "tag" };

export function CategoriesPage() {
  const [rows, setRows] = useState<Category[]>([]);
  const [visible, setVisible] = useState(false);
  const [form, setForm] = useState(empty);

  const load = () => api<Category[]>("/api/categories").then(setRows);

  useEffect(() => {
    load();
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await api("/api/categories", { method: "POST", body: JSON.stringify(form) });
    setVisible(false);
    setForm(empty);
    load();
  };

  return (
    <div className="flex flex-column gap-3">
      <div className="toolbar">
        <div>
          <h2 className="page-title">Kategoriler</h2>
          <p className="page-subtitle">Gelir ve gider sınıfları</p>
        </div>
        <Button label="Yeni kategori" onClick={() => setVisible(true)} />
      </div>
      <div className="panel">
        <DataTable value={rows}>
          <Column field="name" header="Ad" />
          <Column field="kind" header="Tür" body={(row) => <Tag value={row.kind === "INCOME" ? "Gelir" : row.kind === "EXPENSE" ? "Gider" : "Her ikisi"} />} />
          <Column field="color" header="Renk" body={(row) => <span style={{ color: row.color }}>{row.color}</span>} />
        </DataTable>
      </div>
      <Dialog header="Kategori" visible={visible} onHide={() => setVisible(false)}>
        <form className="flex flex-column gap-2" onSubmit={submit}>
          <InputText value={form.name} placeholder="Ad" onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <Dropdown value={form.kind} options={[{ label: "Gider", value: "EXPENSE" }, { label: "Gelir", value: "INCOME" }, { label: "Her ikisi", value: "BOTH" }]} onChange={(e) => setForm({ ...form, kind: e.value })} />
          <InputText value={form.color} onChange={(e) => setForm({ ...form, color: e.target.value })} />
          <Button type="submit" label="Kaydet" />
        </form>
      </Dialog>
    </div>
  );
}
