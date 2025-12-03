/* ========================================
   INITIALISIERUNG UND KONFIGURATION
======================================== */
(() => {
  const API_BASE = '/api/transactions';
  let transactions = [];
  let filtered = [];
  let currentPage = 1;
  let chart = null;

  const $ = sel => document.querySelector(sel);

  /* -----------------------------
     REFERENZEN ELEMENTE DOM
  ----------------------------- */
  const el = {
    searchInput: $('#searchInput'),
    filterType: $('#filterType'),
    sortSelect: $('#sortSelect'),
    pageSize: $('#pageSize'),
    txTableBody: $('#txTable tbody'),
    pagination: $('#pagination'),
    modal: document.getElementById('transactionModal'),
    openModalBtn: $('#openModalBtn'),
    closeModal: $('#closeModal'),
    cancelBtn: $('#cancelBtn'),
    txForm: $('#txForm'),
    txDate: $('#txDate'),
    txType: $('#txType'),
    txCategory: $('#txCategory'),
    txDescription: $('#txDescription'),
    txAmount: $('#txAmount'),
    importBtn: $('#importBtn'),
    importFile: $('#importFile'),
    exportPdfBtn: $('#exportPdfBtn'),          
    importPdfBtn: $('#importPdfBtn'),           
    importPdfFile: $('#importPdfFile'),        
    exportBtn: $('#exportBtn'),
     importChoiceBtn: $('#importChoiceBtn'),
    exportChoiceBtn: $('#exportChoiceBtn'),
    formatModal: $('#formatChoiceModal'),
    chooseExcelBtn: $('#chooseExcelBtn'),
    choosePdfBtn: $('#choosePdfBtn'),
    closeFormatModalBtn: $('#closeFormatModalBtn'),
    showChartsBtn: $('#showChartsBtn'),
    chartControls: $('#chartControls'),
    loadChartBtn: $('#loadChartBtn'),
    chartType: $('#chartType'),
    chartCanvas: $('#chartCanvas'),
    chartTitle: $('#chartTitle'),
    chartTitleWrapper: $('#chartTitleWrapper'),
    filterDateFrom: $('#filterDateFrom'),
    filterDateTo: $('#filterDateTo'),
    filterAmountMin: $('#filterAmountMin'),
    filterAmountMax: $('#filterAmountMax'),
    filterCategory: $('#filterCategory'),
    filterTags: $('#filterTags')
  };

  /* ========================================
     UTILITY - DATUMSFORMAT
  ======================================== */
  function formatDateToUI(iso) {
    if (!iso) return '';
    if (iso.indexOf('-') > -1) {
      const parts = iso.split('-');
      return `${parts[2]}.${parts[1]}.${parts[0]}`;
    }
    return iso;
  }

  function formatDateToISO(dmy) {
    if (!dmy) return '';
    if (dmy.indexOf('-') > -1) return dmy;
    const parts = dmy.split('.').map(p => p.trim());
    if (parts.length !== 3) return dmy;
    return `${parts[2]}-${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}`;
  }

  /* ========================================
    UTILITY – INDIVIDUELLE Alert
  ======================================== */
  function showAlert(message) {
    const box = document.getElementById('customAlert');
    const msg = document.getElementById('alertMessage');
    msg.textContent = message;
    box.classList.remove('hidden');
    document.getElementById('alertOk').onclick = () => box.classList.add('hidden');
  }

  function showConfirm(message) {
    return new Promise(resolve => {
      const box = document.getElementById('customConfirm');
      const msg = document.getElementById('confirmMessage');
      msg.textContent = message;
      box.classList.remove('hidden');
      document.getElementById('confirmYes').onclick = () => { box.classList.add('hidden'); resolve(true); };
      document.getElementById('confirmNo').onclick = () => { box.classList.add('hidden'); resolve(false); };
    });
  }

  /* ========================================
     API - TRANSAKTIONEN LADEN
  ======================================== */
  async function fetchAll() {
    try {
      const res = await fetch(API_BASE);
      transactions = await res.json();
      applyFilters();
      populateCategories();
    } catch (e) {
      console.error(e);
      showAlert('Fehler beim Laden der Transaktionen');
    }
  }

  /* ========================================
     KATEGORIEN – DROPDOWN-MENÜ
  ======================================== */
  function populateCategories() {
    const sel = document.getElementById("filterCategory");
    if (!sel) return;

    const unique = [...new Set(transactions.map(t => t.category).filter(Boolean))];

    sel.innerHTML = `<option value="">Alle</option>`;
    
    unique.forEach(c => {
      const opt = document.createElement("option");
      opt.value = c.toLowerCase();
      opt.textContent = c;
      sel.appendChild(opt);
    });
  }

  /* ========================================
    FILTER – ANWENDUNG ERWEITERTE FILTER
  ======================================== */
  function applyFilters() {
    const q = (el.searchInput?.value || '').toLowerCase();
    const type = el.filterType?.value || '';

    // Erweiterte Filter
    const dateFrom = document.getElementById("filterDateFrom")?.value || "";
    const dateTo = document.getElementById("filterDateTo")?.value || "";
    const amountMin = parseFloat(document.getElementById("filterAmountMin")?.value) || null;
    const amountMax = parseFloat(document.getElementById("filterAmountMax")?.value) || null;
    const categoryFilter = (document.getElementById("filterCategory")?.value || "").toLowerCase();
    const tagsFilter = (document.getElementById("filterTags")?.value || "")
      .toLowerCase()
      .split(",")
      .map(t => t.trim())
      .filter(t => t);

    filtered = transactions.filter(t => {
      // Textsuche (Beschreibung + Kategorie)
      if (q) {
        const desc = (t.description || '').toLowerCase();
        const cat = (t.category || '').toLowerCase();
        if (!desc.includes(q) && !cat.includes(q)) return false;
      }

      // Filtertyp
      if (type && t.type !== type) return false;

      // Datumsfilter
      if (dateFrom && t.date < dateFrom) return false;
      if (dateTo && t.date > dateTo) return false;

      // Betrag filter
      if (amountMin !== null && t.amount < amountMin) return false;
      if (amountMax !== null && t.amount > amountMax) return false;

      // Kategorie-Filter
      if (categoryFilter && t.category?.toLowerCase() !== categoryFilter) return false;

      // Beschreibung-Filter
      if (tagsFilter.length > 0) {
        const text = ((t.description || "") + " " + (t.category || "")).toLowerCase();
        const match = tagsFilter.some(tag => text.includes(tag));
        if (!match) return false;
      }

      return true;
    });

    applySort();
  }

  /* ========================================
     ORDNUNG
  ======================================== */
  function applySort() {
    const s = el.sortSelect?.value || '';
    if (s === 'date_asc') filtered.sort((a,b)=> (a.date||'').localeCompare(b.date||''));
    else if (s === 'date_desc') filtered.sort((a,b)=> (b.date||'').localeCompare(a.date||''));
    else if (s === 'amount_asc') filtered.sort((a,b)=> (a.amount||0)-(b.amount||0));
    else if (s === 'amount_desc') filtered.sort((a,b)=> (b.amount||0)-(a.amount||0));
    renderTable();
  }

  /* ========================================
     SEITENZAHL
  ======================================== */
  function getPageItems() {
    const pageSize = parseInt(el.pageSize?.value || '10',10);
    const start = (currentPage-1)*pageSize;
    return filtered.slice(start, start+pageSize);
  }

  function renderPagination() {
    const pageSize = parseInt(el.pageSize?.value || '10',10);
    const totalPages = Math.ceil((filtered.length||0)/pageSize);
    el.pagination.innerHTML = '';
    if (totalPages<=1) return;
    for (let i=1;i<=totalPages;i++){
      const b = document.createElement('button');
      b.textContent = i;
      b.className = 'page-btn'+(i===currentPage?' active':'');
      b.addEventListener('click', ()=> { currentPage = i; renderTable(); });
      el.pagination.appendChild(b);
    }
  }

  /* ========================================
     TABELLEN-RENDERING
  ======================================== */
  function renderTable() {
    const items = getPageItems();
    el.txTableBody.innerHTML = '';
    items.forEach(tx => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${tx.id||''}</td>
        <td contenteditable="true" data-field="date">${formatDateToUI(tx.date)}</td>
        <td>
          <select data-field="type" class="inline-select">
            <option value="EINNAHMEN" ${tx.type==='EINNAHMEN' ? 'selected' : ''}>EINNAHMEN</option>
            <option value="AUSGABEN" ${tx.type==='AUSGABEN' ? 'selected' : ''}>AUSGABEN</option>
          </select>
        </td>
        <td contenteditable="true" data-field="category">${tx.category||''}</td>
        <td contenteditable="true" data-field="description">${tx.description||''}</td>
        <td contenteditable="true" data-field="amount">${tx.amount!=null?tx.amount:''}</td>
        <td>
          <div class="row-actions">
            <button class="btn save">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17 21 17 13 7 13 7 21"/>
                <polyline points="7 3 7 8 15 8"/>
              </svg>
              Speichern
            </button>
            <button class="btn delete">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              Löschen
            </button>
          </div>
        </td>
      `;
      tr.querySelector('.save').addEventListener('click', ()=> saveRow(tr, tx));
      tr.querySelector('.delete').addEventListener('click', ()=> deleteTx(tx.id));
      el.txTableBody.appendChild(tr);
    });
    renderPagination();
  }

  /* ========================================
     GESAMTBETRÄGE – BERECHNUNG UND AKTUALISIERUNG
  ======================================== */
  function updateTotals() {
    const income = transactions
        .filter(t => t.type === "EINNAHMEN")
        .reduce((sum, t) => sum + Number(t.amount), 0);

    const expense = transactions
        .filter(t => t.type === "AUSGABEN")
        .reduce((sum, t) => sum + Number(t.amount), 0);

    document.getElementById("totalIncome").textContent = income.toFixed(2) + " €";
    document.getElementById("totalExpense").textContent = expense.toFixed(2) + " €";
    document.getElementById("totalBalance").textContent = (income - expense).toFixed(2) + " €";
  }

  /* ========================================
     CRUD - TRANSAKTION SPEICHERN
  ======================================== */
  async function saveRow(tr, tx) {
    const updated = {...tx};
    tr.querySelectorAll('[data-field]').forEach(node => {
      const f = node.getAttribute('data-field');
      if (node.tagName === 'SELECT') updated[f] = node.value;
      else updated[f] = node.textContent.trim();
      if (f === 'date') updated.date = formatDateToISO(updated.date);
    });
    updated.amount = parseFloat(updated.amount) || 0;
    if (!updated.id) { showAlert('Keine ID vorhanden - kann nicht speichern.'); return; }
    try {
      const res = await fetch(`${API_BASE}/${updated.id}`, {
        method:'PUT',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify(updated)
      });
      if (!res.ok) throw new Error('save error');
      const idx = transactions.findIndex(t=>t.id===updated.id);
      if (idx>=0) transactions[idx] = updated;
      applyFilters();
      updateTotals();
      showAlert('Gespeichert');
    } catch(e){
      console.error(e);
      showAlert('Fehler beim Speichern');
    }
  }

  /* ========================================
     CRUD - TRANSAKTION LÖSCHEN
  ======================================== */
  async function deleteTx(id) {
    const ok = await showConfirm('Wirklich löschen?');
    if (!ok) return;
    try {
      const res = await fetch(`${API_BASE}/${id}`, { method:'DELETE' });
      if (!res.ok) throw new Error('del error');
      transactions = transactions.filter(t=>t.id!==id);
      applyFilters();
      updateTotals();
      showAlert('Gelöscht');
    } catch(e){
      console.error(e);
      showAlert('Fehler beim Löschen');
    }
  }

  /* ========================================
     MODAL - ÖFFNEN/SCHLIESSEN VERWALTUNG
  ======================================== */
  function openModal(){ if (el.modal) el.modal.classList.remove('hidden'); }
  function closeModal(){ if (el.modal) el.modal.classList.add('hidden'); if (el.txForm) el.txForm.reset(); }

  /* ========================================
     CRUD - NEUE TRANSAKTION ERSTELLEN
  ======================================== */
  async function onSubmitNewTx(e){
    e.preventDefault();
    const tx = {
      date: formatDateToISO(el.txDate?.value || ''),
      type: el.txType?.value || '',
      category: el.txCategory?.value || '',
      description: el.txDescription?.value || '',
      amount: parseFloat(el.txAmount?.value) || 0
    };
    try {
      const res = await fetch(API_BASE, {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify(tx)
      });
      if (!res.ok) { showAlert('Fehler beim Erstellen'); return; }
      let saved = null;
      try { saved = await res.json(); } catch{}
      if (saved) transactions.push(saved); else await fetchAll();
      closeModal();
      applyFilters();
      updateTotals();
      showAlert('Erstellt');
    } catch(e){ console.error(e); showAlert('Fehler beim Erstellen'); }
  }

  /* ========================================
     IMPORT/EXPORT - IMPORT EXCEL
  ======================================== */
  async function onImportFileChange(e){
    const f = e.target.files[0]; if (!f) return;
    const fd = new FormData(); fd.append('file', f);
    try {
      const res = await fetch(API_BASE + '/import', { method:'POST', body: fd });
      let result;
      try{ result = await res.json(); } catch { result = { success: res.ok, message: await res.text() }; }
      if (!res.ok || !result.success) { showAlert('Import fehlgeschlagen: ' + (result.message||'Unbekannter Fehler')); return; }
      showAlert(result.message || 'Import erfolgreich!');
      await fetchAll();
      updateTotals();
      e.target.value = '';
    } catch(err){ console.error(err); showAlert('Importfehler: ' + err.message); }
  }

  /* ========================================
     IMPORT/EXPORT - EXPORT EXCEL
  ======================================== */
  async function onExportClick(){
    try {
      const res = await fetch(API_BASE + '/export');
      if (!res.ok) { showAlert('Export fehlgeschlagen'); return; }
      const blob = await res.blob(); 
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); 
      a.href = url; 
      a.download = 'transactions.xlsx'; 
      document.body.appendChild(a); 
      a.click(); 
      a.remove(); 
      URL.revokeObjectURL(url);
      showAlert('Export fertig');
    } catch(e){ console.error(e); showAlert('Export fehlgeschlagen'); }
  }


  /* ========================================
   PDF EXPORT 
======================================== */
async function onExportPdfClick(){
  try {
    // Mostra un loader opzionale
    showAlert('PDF wird erstellt...');
    
    const res = await fetch(API_BASE + '/export/pdf');
    
    if (!res.ok) { 
      showAlert('PDF Export fehlgeschlagen'); 
      return; 
    }
    
    const blob = await res.blob(); 
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); 
    a.href = url; 
    a.download = 'transactions.pdf'; 
    document.body.appendChild(a); 
    a.click(); 
    a.remove(); 
    URL.revokeObjectURL(url);
    
    showAlert('PDF Export erfolgreich!');
  } catch(e){ 
    console.error(e); 
    showAlert('PDF Export fehlgeschlagen: ' + e.message); 
  }
}

/* ========================================
   PDF IMPORT
======================================== */
async function onImportPdfFileChange(e){
  const file = e.target.files[0]; 
  if (!file) return;
  
  // Validazione del file
  if (!file.name.toLowerCase().endsWith('.pdf')) {
    showAlert('Bitte wählen Sie eine PDF-Datei aus');
    e.target.value = '';
    return;
  }
  
  const fd = new FormData(); 
  fd.append('file', file);
  
  try {
    showAlert('PDF wird importiert...');
    
    const res = await fetch(API_BASE + '/import/pdf', { 
      method: 'POST', 
      body: fd 
    });
    
    let result;
    try { 
      result = await res.json(); 
    } catch { 
      result = { 
        success: res.ok, 
        message: await res.text() 
      }; 
    }
    
    if (!res.ok || !result.success) { 
      showAlert('PDF Import fehlgeschlagen: ' + (result.message || 'Unbekannter Fehler')); 
      return; 
    }
    
    showAlert(result.message || 'PDF Import erfolgreich!');
    await fetchAll();
    updateTotals();
    e.target.value = '';
  } catch(err) { 
    console.error(err); 
    showAlert('PDF Importfehler: ' + err.message); 
  }
}

  /* ========================================
     GRAFIKEN - TOGGLE KONTROLLFELD
  ======================================== */
  function toggleChartControls(){
    if (!el.chartControls) return;
    el.chartControls.classList.toggle('hidden');
    if (el.chartCanvas) el.chartCanvas.classList.add('hidden');
    if (el.chartTitleWrapper) el.chartTitleWrapper.classList.add('hidden');
  }

  /* ========================================
     GRAFIKEN – LADEN UND RENDERN
  ======================================== */
  function loadChart(type){
    if (!el.chartCanvas) return;
    el.chartCanvas.classList.remove('hidden');
    if (el.chartTitleWrapper) el.chartTitleWrapper.classList.remove('hidden');
    
    // Einstellung des Diagrammtitels
    const titles = {
      'Ausgaben': 'Monatliche Ausgaben',
      'Einnahmen': 'Monatliche Einnahmen',
      'AusgabenEinnahmen': 'Ausgaben vs Einnahmen',
      'Kontostand': 'Kontostand Verlauf',
      'Kategorie': 'Ausgaben nach Kategorie'
    };
    if (el.chartTitle) el.chartTitle.textContent = titles[type] || 'Grafik';
    
    if (chart) chart.destroy();

    const dates = [...new Set(transactions.map(t=>t.date))].sort();
    
    // Grafik Ausgaben
    if (type === 'Ausgaben') {
      const data = transactions.filter(t=>t.type==='AUSGABEN').map(t=>Math.abs(t.amount));
      chart = new Chart(el.chartCanvas,{ 
        type:'bar', 
        data:{ 
          labels:data.map((_,i)=>i+1), 
          datasets:[{ 
            label:'Ausgaben', 
            data,
            backgroundColor: 'rgba(255,99,132,0.6)',
            borderColor: 'rgba(255,99,132,1)',
            borderWidth: 2
          }] 
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: true } }
        }
      });
      return;
    }
    
    // Grafik Einnahmen
    if (type === 'Einnahmen') {
      const data = transactions.filter(t=>t.type==='EINNAHMEN').map(t=>Math.abs(t.amount));
      chart = new Chart(el.chartCanvas,{ 
        type:'bar', 
        data:{ 
          labels:data.map((_,i)=>i+1), 
          datasets:[{ 
            label:'Einnahmen', 
            data,
            backgroundColor: 'rgba(75,192,192,0.6)',
            borderColor: 'rgba(75,192,192,1)',
            borderWidth: 2
          }] 
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: true } }
        }
      });
      return;
    }
    
    // Grafik Kontostand
    if (type === 'Kontostand') {
      const labels = transactions.map(t=>t.date);
      const data = transactions.reduce((acc,t)=>{ 
        acc.push((acc.length?acc[acc.length-1]:0)+t.amount); 
        return acc; 
      },[]);
      chart = new Chart(el.chartCanvas,{ 
        type:'line', 
        data:{ 
          labels, 
          datasets:[{ 
            label:'Kontostand', 
            data,
            borderColor: 'rgba(54, 162, 235, 1)',
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            tension: 0.4,
            fill: true
          }] 
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: false } }
        }
      });
      return;
    }
    
    // Grafik Kategorie
    if (type === 'Kategorie') {
      const map = {}; 
      transactions.forEach(t=>{ 
        map[t.category] = (map[t.category]||0)+Math.abs(t.amount); 
      });
      const labels = Object.keys(map); 
      const data = labels.map(l=>map[l]);
      chart = new Chart(el.chartCanvas,{ 
        type:'pie', 
        data:{ 
          labels, 
          datasets:[{ 
            data,
            backgroundColor: [
              'rgba(255, 99, 132, 0.8)',
              'rgba(54, 162, 235, 0.8)',
              'rgba(255, 206, 86, 0.8)',
              'rgba(75, 192, 192, 0.8)',
              'rgba(153, 102, 255, 0.8)',
              'rgba(255, 159, 64, 0.8)'
            ]
          }] 
        }, 
        options:{ 
          responsive: true,
          maintainAspectRatio: false
        } 
      });
      return;
    }
    
    // Grafik Ausgaben vs Einnahmen
    if (type === 'AusgabenEinnahmen') {
      const ausgabenData = dates.map(date=> 
        transactions.filter(t=>t.date===date && t.type==='AUSGABEN')
          .reduce((s,t)=>s+Math.abs(t.amount),0) 
      );
      const einnahmenData = dates.map(date=> 
        transactions.filter(t=>t.date===date && t.type==='EINNAHMEN')
          .reduce((s,t)=>s+Math.abs(t.amount),0) 
      );
      chart = new Chart(el.chartCanvas,{ 
        type:'bar', 
        data:{ 
          labels:dates, 
          datasets:[
            { 
              label:'Ausgaben', 
              data:ausgabenData,
              backgroundColor: 'rgba(255,99,132,0.6)',
              borderColor: 'rgba(255,99,132,1)',
              borderWidth: 2
            },
            { 
              label:'Einnahmen', 
              data:einnahmenData,
              backgroundColor: 'rgba(75,192,192,0.6)',
              borderColor: 'rgba(75,192,192,1)',
              borderWidth: 2
            }
          ] 
        }, 
        options:{ 
          responsive: true,
          maintainAspectRatio: false,
          scales:{ y:{ beginAtZero:true } } 
        } 
      });
      return;
    }
  }

  /* ========================================
     EVENT LISTENERS - BINDING UI
  ======================================== */
  function bindUI(){
   
    if (el.searchInput) el.searchInput.addEventListener('input', ()=> { currentPage = 1; applyFilters(); });
    if (el.filterType) el.filterType.addEventListener('change', ()=> { currentPage = 1; applyFilters(); });
    if (el.sortSelect) el.sortSelect.addEventListener('change', ()=> { currentPage = 1; applySort(); });
    if (el.pageSize) el.pageSize.addEventListener('change', ()=> { currentPage = 1; renderTable(); });

    // Modal
    if (el.openModalBtn) el.openModalBtn.addEventListener('click', openModal);
    if (el.closeModal) el.closeModal.addEventListener('click', closeModal);
    if (el.cancelBtn) el.cancelBtn.addEventListener('click', closeModal);
    window.addEventListener('click', e=> { if (e.target === el.modal) closeModal(); });

    // Form neue Transaktion
    if (el.txForm) el.txForm.addEventListener('submit', onSubmitNewTx);


// Open modal Import
if(el.importChoiceBtn) {
    el.importChoiceBtn.addEventListener('click', () => {
        el.formatModal.classList.remove('hidden');
        el.formatModal.dataset.action = 'import'; 
    });
}

// Open modal Export
if(el.exportChoiceBtn) {
    el.exportChoiceBtn.addEventListener('click', () => {
        el.formatModal.classList.remove('hidden');
        el.formatModal.dataset.action = 'export'; 
    });
}

// Button Excel
if(el.chooseExcelBtn) {
    el.chooseExcelBtn.addEventListener('click', () => {
        const action = el.formatModal.dataset.action;
        if(action === 'import') {
            el.importBtn && el.importBtn.click();
        } else if(action === 'export') {
            el.exportBtn && el.exportBtn.click();
        }
        el.formatModal.classList.add('hidden');
    });
}

// Button PDF
if(el.choosePdfBtn) {
    el.choosePdfBtn.addEventListener('click', () => {
        const action = el.formatModal.dataset.action;
        if(action === 'import') {
            el.importPdfFile && el.importPdfFile.click();
        } else if(action === 'export') {
            onExportPdfClick();
        }
        el.formatModal.classList.add('hidden');
    });
}

// close modal
if(el.closeFormatModalBtn) {
    el.closeFormatModalBtn.addEventListener('click', () => {
        el.formatModal.classList.add('hidden');
    });
}


    // Import/Export excel
    if (el.importBtn) el.importBtn.addEventListener('click', ()=> el.importFile && el.importFile.click());
    if (el.importFile) el.importFile.addEventListener('change', onImportFileChange);
    if (el.exportBtn) el.exportBtn.addEventListener('click', onExportClick);

    // Import/Export PDF
    if (el.importPdfBtn) el.importPdfBtn.addEventListener('click', () => el.importPdfFile && el.importPdfFile.click());
    if (el.importPdfFile) el.importPdfFile.addEventListener('change', onImportPdfFileChange);
    if (el.exportPdfBtn) el.exportPdfBtn.addEventListener('click', onExportPdfClick);


    // Grafici
    if (el.showChartsBtn) el.showChartsBtn.addEventListener('click', toggleChartControls);
    if (el.loadChartBtn) el.loadChartBtn.addEventListener('click', ()=> loadChart(el.chartType?.value));

    /* -----------------------------
       ERWEITERTE FILTER
    ----------------------------- */
    const advBtn = document.getElementById("advancedFiltersBtn");
    const advPanel = document.getElementById("advancedFiltersPanel");

    if (advBtn && advPanel) {
      advBtn.addEventListener("click", () => {
        advPanel.classList.toggle("hidden");
      });
    }

    // Listener Erweiterte Filter
    [
      "filterDateFrom",
      "filterDateTo",
      "filterAmountMin",
      "filterAmountMax",
      "filterCategory",
      "filterTags"
    ].forEach(id => {
      const field = document.getElementById(id);
      if (field) field.addEventListener("input", () => {
        currentPage = 1;
        applyFilters();
      });
    });
  }

  /* ========================================
     THEME TOGGLE - DUNKEL/HELL-MODUS
  ======================================== */
  (function themeInit(){
    const root = document.documentElement;
    const btn = document.getElementById('toggleTheme');
    const dot = document.getElementById('themeDot');
    const saved = localStorage.getItem('theme');
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    
    function apply(mode){ 
      if (mode==='dark') root.classList.add('dark'); 
      else root.classList.remove('dark');
    }
    
    if (saved) apply(saved); 
    else apply(prefersDark ? 'dark' : 'light');
    
    if (btn) btn.addEventListener('click', ()=>{ 
      const now = root.classList.contains('dark') ? 'light' : 'dark'; 
      localStorage.setItem('theme', now); 
      apply(now); 
    });
    
    if (window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e=>{ 
        const saved = localStorage.getItem('theme'); 
        if(!saved) apply(e.matches? 'dark' : 'light'); 
      });
    }
  })();

  /* ========================================
     ANWENDUNG INITIALISIEREN
  ======================================== */
  function init(){ 
    bindUI(); 
    fetchAll();
    updateTotals();
  }
  init();

})();