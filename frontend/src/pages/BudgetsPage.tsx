import { FormEvent, useEffect, useState } from "react";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputNumber } from "primereact/inputnumber";
import { api } from "../api/client";
import type { Budget, Category } from "../api/types";
import { money } from "../format";

export function BudgetsPage() {
  const year = new Date().getFullYear();
  const [rows, setRows] = useState<Budget[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [visible, setVisible] = useState(false);
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [month, setMonth] = useState<number | null>(new Date().getMonth() + 1);
  const [amount, setAmount] = useState(0);

  const load = () => api<Budget[]>(`/api/budgets?year=${year}`).then(setRows);

  useEffect(() => {
    load();
    api<Category[]>("/api/categories").then(setCategories);
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await api("/api/budgets", {
      method: "POST",
      body: JSON.stringify({ categoryId, year, month, amount, currency: "TRY" }),
    });
    setVisible(false);
    load();
  };

  return (
    <div className="flex flex-column gap-3">
      <div className="toolbar">
        <div>
          <h2 className="page-title">Bütçeler</h2>
          <p className="page-subtitle">{year} yılı limitleri</p>
        </div>
        <Button label="Bütçe ekle" onClick={() => setVisible(true)} />
      </div>
      <div className="panel">
        <DataTable value={rows} emptyMessage="Bütçe yok.">
          <Column field="categoryName" header="Kategori" />
          <Column field="month" header="Ay" body={(row) => row.month ?? "Yıllık"} />
          <Column field="amount" header="Limit" body={(row) => money(row.amount, row.currency)} />
          <Column header="" body={(row: Budget) => <Button icon="pi pi-trash" text severity="danger" onClick={async () => { await api(`/api/budgets/${row.id}`, { method: "DELETE" }); load(); }} />} />
        </DataTable>
      </div>
      <Dialog header="Bütçe" visible={visible} onHide={() => setVisible(false)}>
        <form className="flex flex-column gap-2" onSubmit={submit}>
          <Dropdown value={categoryId} options={categories} optionLabel="name" optionValue="id" placeholder="Genel veya kategori" showClear onChange={(e) => setCategoryId(e.value)} />
          <Dropdown value={month} options={Array.from({ length: 12 }, (_, i) => ({ label: `${i + 1}. ay`, value: i + 1 }))} showClear placeholder="Ay" onChange={(e) => setMonth(e.value)} />
          <InputNumber value={amount} mode="currency" currency="TRY" locale="tr-TR" onValueChange={(e) => setAmount(e.value ?? 0)} />
          <Button type="submit" label="Kaydet" />
        </form>
      </Dialog>
    </div>
  );
}
