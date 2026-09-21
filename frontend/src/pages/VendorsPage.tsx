import { FormEvent, useEffect, useState } from "react";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { api, toQuery } from "../api/client";
import type { PageResponse, PartyType, Vendor } from "../api/types";
import { partyLabel } from "../format";

const empty = { name: "", taxNumber: "", email: "", phone: "", iban: "", address: "", partyType: "BOTH" as PartyType, notes: "" };

export function VendorsPage() {
  const [rows, setRows] = useState<Vendor[]>([]);
  const [visible, setVisible] = useState(false);
  const [form, setForm] = useState(empty);
  const [editingId, setEditingId] = useState<string | null>(null);

  const load = () => {
    api<PageResponse<Vendor>>(`/api/vendors${toQuery({ size: 100 })}`).then((data) => setRows(data.content));
  };

  useEffect(() => {
    load();
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await api(editingId ? `/api/vendors/${editingId}` : "/api/vendors", {
      method: editingId ? "PUT" : "POST",
      body: JSON.stringify(form),
    });
    setVisible(false);
    setForm(empty);
    setEditingId(null);
    load();
  };

  return (
    <div className="flex flex-column gap-3">
      <div className="toolbar">
        <div>
          <h2 className="page-title">Cariler</h2>
          <p className="page-subtitle">Tedarikçi ve müşterileriniz</p>
        </div>
        <Button label="Yeni cari" icon="pi pi-plus" onClick={() => { setEditingId(null); setForm(empty); setVisible(true); }} />
      </div>
      <div className="panel">
        <DataTable value={rows} emptyMessage="Cari yok.">
          <Column field="name" header="Ad" />
          <Column field="taxNumber" header="VKN / TCKN" />
          <Column field="partyType" header="Tür" body={(row) => partyLabel[row.partyType]} />
          <Column field="iban" header="IBAN" />
          <Column
            header=""
            body={(row: Vendor) => (
              <div className="flex gap-2">
                <Button icon="pi pi-pencil" text onClick={() => { setEditingId(row.id); setForm({ ...empty, ...row }); setVisible(true); }} />
                <Button icon="pi pi-trash" text severity="danger" onClick={async () => { await api(`/api/vendors/${row.id}`, { method: "DELETE" }); load(); }} />
              </div>
            )}
          />
        </DataTable>
      </div>
      <Dialog header={editingId ? "Cari düzenle" : "Yeni cari"} visible={visible} onHide={() => setVisible(false)} style={{ width: "32rem" }}>
        <form className="flex flex-column gap-2" onSubmit={submit}>
          <InputText value={form.name} placeholder="Ad" onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <InputText value={form.taxNumber} placeholder="VKN / TCKN" onChange={(e) => setForm({ ...form, taxNumber: e.target.value })} />
          <Dropdown value={form.partyType} options={Object.entries(partyLabel).map(([value, label]) => ({ value, label }))} onChange={(e) => setForm({ ...form, partyType: e.value })} />
          <InputText value={form.iban} placeholder="IBAN" onChange={(e) => setForm({ ...form, iban: e.target.value })} />
          <InputText value={form.email} placeholder="E-posta" onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <InputText value={form.phone} placeholder="Telefon" onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <Button type="submit" label="Kaydet" />
        </form>
      </Dialog>
    </div>
  );
}
