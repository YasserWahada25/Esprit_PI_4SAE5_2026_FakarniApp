import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyseIRMResponse, DetectionService, DossierMedicalResponse } from '../services/detection';

@Component({
  selector: 'app-follow-up',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './follow-up.component.html',
  styleUrl: './follow-up.component.css'
})
export class FollowUpComponent implements OnInit {

  /* ── State ─────────────────────────────────────────── */
  activeTab: 'overview' | 'dossier' | 'compare' | 'stats' = 'overview';
  dossier: DossierMedicalResponse | null = null;
  isLoading = false;
  errorMessage = '';
  readonly patientId = 1;
  showAlert = true;

  /* ── Edit modal ─────────────────────────────────────── */
  editingAnalyse: AnalyseIRMResponse | null = null;
  editForm = { descriptionRisque: '', conseilMedecin: '', notesCliniques: '' };

  /* ── Compare feature ────────────────────────────────── */
  compareSelection: AnalyseIRMResponse[] = [];

  /* ── MRI Viewer ─────────────────────────────────────── */
  viewerAnalyse: AnalyseIRMResponse | null = null;
  viewerZoom        = 1;
  viewerRotation    = 0;
  viewerFlipped     = false;
  viewerContrast    = 100;
  viewerBrightness  = 100;
  viewerSaturation  = 100;

  constructor(private detectionService: DetectionService) {}

  ngOnInit(): void {
    this.loadDossier();
  }

  /* ════════════════════════════════════════════════════════
     DATA LOADING
  ════════════════════════════════════════════════════════ */
  loadDossier(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.showAlert = true;
    this.detectionService.getDossierByPatientId(this.patientId).subscribe({
      next: (dossier) => {
        dossier.analyses.sort((a, b) =>
          new Date(b.dateAnalyse).getTime() - new Date(a.dateAnalyse).getTime()
        );
        this.dossier = dossier;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 404
          ? 'No medical record found for this patient.'
          : 'Error loading dossier. Please try again.';
      }
    });
  }

  /* ════════════════════════════════════════════════════════
     MRI IMAGE URL
  ════════════════════════════════════════════════════════ */
  getMriImageUrl(nomFichier: string): string {
    return `http://localhost:8058/api/detection/image/${nomFichier}`;
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/mri-placeholder.png';
    img.style.opacity = '0.3';
    img.style.filter = 'grayscale(1)';
  }

  /* ════════════════════════════════════════════════════════
     VIEWER
  ════════════════════════════════════════════════════════ */
  openViewer(analyse: AnalyseIRMResponse): void {
    this.viewerAnalyse   = analyse;
    this.viewerZoom      = 1;
    this.viewerRotation  = 0;
    this.viewerFlipped   = false;
    this.viewerContrast  = 100;
    this.viewerBrightness = 100;
    this.viewerSaturation = 100;
  }
  closeViewer(): void { this.viewerAnalyse = null; }

  zoomIn():           void { this.viewerZoom = Math.min(5, +(this.viewerZoom + 0.25).toFixed(2)); }
  zoomOut():          void { this.viewerZoom = Math.max(0.25, +(this.viewerZoom - 0.25).toFixed(2)); }
  rotateImage():      void { this.viewerRotation = (this.viewerRotation + 90) % 360; }
  flipImage():        void { this.viewerFlipped = !this.viewerFlipped; }
  increaseContrast(): void { this.viewerContrast   = Math.min(200, this.viewerContrast + 20); }
  increaseBrightness(): void { this.viewerBrightness = Math.min(200, this.viewerBrightness + 20); }
  resetViewer():      void {
    this.viewerZoom = 1; this.viewerRotation = 0; this.viewerFlipped = false;
    this.viewerContrast = 100; this.viewerBrightness = 100; this.viewerSaturation = 100;
  }

  getViewerTransform(): string {
    const flip = this.viewerFlipped ? ' scaleX(-1)' : '';
    return `scale(${this.viewerZoom}) rotate(${this.viewerRotation}deg)${flip}`;
  }
  getViewerFilter(): string {
    return `contrast(${this.viewerContrast}%) brightness(${this.viewerBrightness}%) saturate(${this.viewerSaturation}%)`;
  }

  /* ════════════════════════════════════════════════════════
     COMPARE FEATURE
  ════════════════════════════════════════════════════════ */
  toggleCompareSelection(analyse: AnalyseIRMResponse): void {
    const idx = this.compareSelection.findIndex(a => a.id === analyse.id);
    if (idx >= 0) {
      this.compareSelection.splice(idx, 1);
    } else {
      if (this.compareSelection.length >= 2) {
        this.compareSelection.shift(); // replace oldest
      }
      this.compareSelection.push(analyse);
    }
  }

  isSelectedForCompare(id: number): boolean {
    return this.compareSelection.some(a => a.id === id);
  }

  /**
   * Returns a diff table between the two selected scans.
   */
  getCompareDiff(): Array<{label:string, valA:string, valB:string, colorA:string, colorB:string, change:string, changeClass:string}> {
    if (this.compareSelection.length < 2) return [];
    const [a, b] = this.compareSelection;
    const stages = ['Non_Demented','Very_Mild_Demented','Mild_Demented','Moderate_Demented'];
    const stageA = stages.indexOf(a.prediction);
    const stageB = stages.indexOf(b.prediction);
    const stageDelta = stageB - stageA;

    const confDelta = b.confidence - a.confidence;
    const mmseA = this.predictionToScore(a.prediction);
    const mmseB = this.predictionToScore(b.prediction);
    const mmseDelta = mmseB - mmseA;

    return [
      {
        label: 'Stage',
        valA: `Stage ${stageA + 1}`,
        valB: `Stage ${stageB + 1}`,
        colorA: this.getStageColor(a.prediction),
        colorB: this.getStageColor(b.prediction),
        change: stageDelta === 0 ? '—' : (stageDelta > 0 ? `+${stageDelta}` : `${stageDelta}`),
        changeClass: stageDelta > 0 ? 'positive' : stageDelta < 0 ? 'negative' : 'neutral'
      },
      {
        label: 'Diagnosis',
        valA: this.formatPrediction(a.prediction),
        valB: this.formatPrediction(b.prediction),
        colorA: this.getStageColor(a.prediction),
        colorB: this.getStageColor(b.prediction),
        change: stageDelta === 0 ? 'No change' : (stageDelta > 0 ? '⚠ Worsened' : '✓ Improved'),
        changeClass: stageDelta > 0 ? 'positive' : stageDelta < 0 ? 'negative' : 'neutral'
      },
      {
        label: 'Confidence',
        valA: `${a.confidence.toFixed(1)}%`,
        valB: `${b.confidence.toFixed(1)}%`,
        colorA: this.getStageColor(a.prediction),
        colorB: this.getStageColor(b.prediction),
        change: confDelta === 0 ? '—' : (confDelta > 0 ? `+${confDelta.toFixed(1)}%` : `${confDelta.toFixed(1)}%`),
        changeClass: 'neutral'
      },
      {
        label: 'MMSE Score (est.)',
        valA: `${mmseA}/30`,
        valB: `${mmseB}/30`,
        colorA: this.getStageColor(a.prediction),
        colorB: this.getStageColor(b.prediction),
        change: mmseDelta === 0 ? '—' : (mmseDelta > 0 ? `+${mmseDelta} pts` : `${mmseDelta} pts`),
        changeClass: mmseDelta < 0 ? 'positive' : mmseDelta > 0 ? 'negative' : 'neutral'
      },
      {
        label: 'Risk Level',
        valA: a.niveauRisque || '—',
        valB: b.niveauRisque || '—',
        colorA: this.getStageColor(a.prediction),
        colorB: this.getStageColor(b.prediction),
        change: a.niveauRisque === b.niveauRisque ? 'Same' : 'Changed',
        changeClass: 'neutral'
      }
    ];
  }

  /* ════════════════════════════════════════════════════════
     EDIT / DELETE
  ════════════════════════════════════════════════════════ */
  openEditModal(analyse: AnalyseIRMResponse): void {
    this.editingAnalyse = analyse;
    this.editForm = {
      descriptionRisque: analyse.descriptionRisque || '',
      conseilMedecin: (analyse as any).conseilMedecin || '',
      notesCliniques: (analyse as any).notesCliniques || ''
    };
  }
  closeEditModal(): void { this.editingAnalyse = null; }

  saveEdit(): void {
    if (!this.editingAnalyse) return;
    const request = {
      analyseId: this.editingAnalyse.id,
      descriptionRisque: this.editForm.descriptionRisque,
      conseilMedecin: this.editForm.conseilMedecin,
      notesCliniques: this.editForm.notesCliniques
    };
    this.detectionService.updateAnalyseDescription(request).subscribe({
      next: (updatedDossier) => {
        this.dossier = updatedDossier;
        this.closeEditModal();
      },
      error: (err) => { alert('Error saving: ' + err.message); }
    });
  }

  deleteAnalyse(id: number, nomFichier: string): void {
    if (!confirm(`Delete analysis "${nomFichier}"? This action is irreversible.`)) return;
    this.detectionService.deleteAnalyse(id).subscribe({
      next: (updatedDossier) => { this.dossier = updatedDossier; },
      error: (err) => { alert('Error deleting: ' + err.message); }
    });
  }

  /* ════════════════════════════════════════════════════════
     PRINT / EXPORT
  ════════════════════════════════════════════════════════ */
  printReport(): void { window.print(); }

  exportDossier(): void {
    if (!this.dossier) return;
    const data = JSON.stringify(this.dossier, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href = url;
    a.download = `dossier-patient-${this.patientId}-${new Date().toISOString().slice(0,10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }

  /* ════════════════════════════════════════════════════════
     STAGE PALETTE  (4 clinical colours)
     I   Non Demented       → #38bdf8 (teal)
     II  Very Mild Demented → #818cf8 (blue)
     III Mild Demented      → #c084fc (violet)
     IV  Moderate Demented  → #fb7185 (red)
  ════════════════════════════════════════════════════════ */
  getStageColor(p: string): string {
    return ({
      Non_Demented:       '#38bdf8',   /* Stage I   — Teal   */
      Very_Mild_Demented: '#818cf8',   /* Stage II  — Blue   */
      Mild_Demented:      '#c084fc',   /* Stage III — Violet */
      Moderate_Demented:  '#fb7185'    /* Stage IV  — Red    */
    } as any)[p] || '#64748b';
  }

  getStageGradient(p: string): string {
    return ({
      Non_Demented:       'linear-gradient(90deg,#38bdf8,#22d3ee)',
      Very_Mild_Demented: 'linear-gradient(90deg,#818cf8,#60a5fa)',
      Mild_Demented:      'linear-gradient(90deg,#c084fc,#a78bfa)',
      Moderate_Demented:  'linear-gradient(90deg,#fb7185,#f87171)'
    } as any)[p] || 'linear-gradient(90deg,#64748b,#94a3b8)';
  }

  getStageIcon(p: string): string {
    return ({
      Non_Demented:       'fa-solid fa-shield-heart',
      Very_Mild_Demented: 'fa-solid fa-magnifying-glass-chart',
      Mild_Demented:      'fa-solid fa-triangle-exclamation',
      Moderate_Demented:  'fa-solid fa-circle-exclamation'
    } as any)[p] || 'fa-solid fa-brain';
  }

  getStageNumber(p: string): number {
    return ({
      Non_Demented: 1, Very_Mild_Demented: 2, Mild_Demented: 3, Moderate_Demented: 4
    } as any)[p] || 0;
  }

  /** CSS class for stage pill colour variant */
  getStagePillClass(p: string): string {
    return ({
      Non_Demented:       's1-pill',
      Very_Mild_Demented: 's2-pill',
      Mild_Demented:      's3-pill',
      Moderate_Demented:  's4-pill'
    } as any)[p] || '';
  }

  /* ════════════════════════════════════════════════════════
     ALERT HELPERS
  ════════════════════════════════════════════════════════ */
  getAlertTitle(): string {
    if (!this.dossier) return '';
    const map: Record<string,string> = {
      Non_Demented:       'Patient is cognitively normal — continue routine monitoring',
      Very_Mild_Demented: 'Very mild cognitive impairment detected — increased monitoring recommended',
      Mild_Demented:      'Mild dementia detected — clinical intervention required',
      Moderate_Demented:  'Moderate dementia detected — urgent specialist review required'
    };
    return map[this.dossier.dernierePrediction] || 'Clinical review required';
  }

  getAlertDescription(): string {
    if (!this.dossier) return '';
    const map: Record<string,string> = {
      Non_Demented:       'No significant atrophy detected. Schedule next follow-up in 12 months.',
      Very_Mild_Demented: 'Subtle hippocampal volume reduction noted. Neuropsychological evaluation advised.',
      Mild_Demented:      'Significant cortical atrophy detected. Medication review and specialist consultation required.',
      Moderate_Demented:  'Severe cortical atrophy. Immediate multidisciplinary team assessment and care planning required.'
    };
    return map[this.dossier.dernierePrediction] || '';
  }

  /* ════════════════════════════════════════════════════════
     RECOMMENDATIONS
  ════════════════════════════════════════════════════════ */
  getRecommendations(p: string): Array<{title:string, detail:string, icon:string, color:string, priority:string}> {
    const recs: Record<string, Array<{title:string, detail:string, icon:string, color:string, priority:string}>> = {
      Non_Demented: [
        { title: 'Routine Follow-up', detail: 'Schedule next MRI in 12 months', icon: 'fa-solid fa-calendar-check', color: '#38bdf8', priority: 'ROUTINE' },
        { title: 'Lifestyle Counselling', detail: 'Mediterranean diet, physical activity 150 min/wk', icon: 'fa-solid fa-heart-pulse', color: '#818cf8', priority: 'PREVENTIVE' },
        { title: 'Cognitive Exercises', detail: 'Recommend brain training program', icon: 'fa-solid fa-puzzle-piece', color: '#6366f1', priority: 'ROUTINE' }
      ],
      Very_Mild_Demented: [
        { title: 'Increased Monitoring', detail: 'Follow-up MRI in 6 months', icon: 'fa-solid fa-brain', color: '#818cf8', priority: 'SCHEDULED' },
        { title: 'Neuropsychological Eval.', detail: 'Full battery within 30 days', icon: 'fa-solid fa-clipboard-list', color: '#c084fc', priority: 'SCHEDULED' },
        { title: 'Blood Panel', detail: 'B12, thyroid, homocysteine levels', icon: 'fa-solid fa-vial', color: '#6366f1', priority: 'ROUTINE' }
      ],
      Mild_Demented: [
        { title: 'Specialist Referral', detail: 'Neurology consultation within 2 weeks', icon: 'fa-solid fa-user-doctor', color: '#c084fc', priority: 'URGENT' },
        { title: 'Medication Review', detail: 'Consider ChEI therapy (donepezil)', icon: 'fa-solid fa-pills', color: '#c084fc', priority: 'URGENT' },
        { title: 'Safety Assessment', detail: 'Home environment and driving evaluation', icon: 'fa-solid fa-house-medical', color: '#fb7185', priority: 'PRIORITY' },
        { title: 'Caregiver Support', detail: 'Family education and community resources', icon: 'fa-solid fa-people-group', color: '#64748b', priority: 'ROUTINE' }
      ],
      Moderate_Demented: [
        { title: 'Urgent MDT Review', detail: 'Multidisciplinary team within 48 hours', icon: 'fa-solid fa-hospital', color: '#fb7185', priority: 'CRITICAL' },
        { title: 'Full-time Care Assessment', detail: 'Evaluate care home or intensive home support', icon: 'fa-solid fa-bed-pulse', color: '#fb7185', priority: 'CRITICAL' },
        { title: 'Advanced Care Planning', detail: 'Document preferences and legal capacity', icon: 'fa-solid fa-file-medical', color: '#c084fc', priority: 'URGENT' }
      ]
    };
    return recs[p] || [];
  }

  /* ════════════════════════════════════════════════════════
     GAUGE
  ════════════════════════════════════════════════════════ */
  /** Semicircle arc length ≈ 188 for r=60, stroke-dashoffset to indicate score */
  getGaugeDashOffset(): number {
    if (!this.dossier) return 188;
    const score  = this.getCurrentMmseScore();
    const pct    = score / 30;            // 0–1
    return 188 - (188 * pct);            // offset: 188 = empty, 0 = full
  }

  getCurrentMmseScore(): number {
    if (!this.dossier?.analyses?.length) return 0;
    return this.predictionToScore(this.dossier.analyses[0].prediction);
  }

  /* ════════════════════════════════════════════════════════
     PROBABILITY BARS
  ════════════════════════════════════════════════════════ */
  getProbBars(analyse: AnalyseIRMResponse) {
    return [
      { label: 'Non Demented',       value: analyse.probNonDemented,      color: '#38bdf8' },
      { label: 'Very Mild Demented', value: analyse.probVeryMildDemented, color: '#818cf8' },
      { label: 'Mild Demented',      value: analyse.probMildDemented,     color: '#c084fc' },
      { label: 'Moderate Demented',  value: analyse.probModerateDemented, color: '#fb7185' }
    ];
  }

  /* ════════════════════════════════════════════════════════
     STATS
  ════════════════════════════════════════════════════════ */
  predictionToScore(p: string): number {
    return ({ Non_Demented: 28, Very_Mild_Demented: 22, Mild_Demented: 16, Moderate_Demented: 9 } as any)[p] ?? 20;
  }

  get mmseHistory() {
    if (!this.dossier?.analyses?.length) return [];
    return [...this.dossier.analyses].reverse().slice(-8).map(a => ({
      date:  this.shortDate(a.dateAnalyse),
      score: this.predictionToScore(a.prediction),
      color: this.getStageColor(a.prediction)
    }));
  }

  getBarHeight(score: number): string {
    return (score / 30 * 100) + '%';
  }

  get cognitiveStatus(): string {
    if (!this.dossier || this.dossier.analyses.length < 2) return 'Insufficient data';
    const stages = ['Non_Demented','Very_Mild_Demented','Mild_Demented','Moderate_Demented'];
    const d = stages.indexOf(this.dossier.analyses[0].prediction) -
              stages.indexOf(this.dossier.analyses[1].prediction);
    return d > 0 ? 'Declining' : d < 0 ? 'Improving' : 'Stable';
  }

  get cognitiveStatusColor(): string {
    return ({
      Declining:           '#fb7185',
      Improving:           '#38bdf8',
      Stable:              '#818cf8',
      'Insufficient data': '#7c6fad'
    } as any)[this.cognitiveStatus] || '#7c6fad';
  }

  get stageDistribution() {
    if (!this.dossier?.analyses?.length) return [];
    const t = this.dossier.analyses.length;
    return [
      { key: 'Non_Demented',       label: 'Non Demented', color: '#38bdf8', icon: 'fa-solid fa-shield-heart' },
      { key: 'Very_Mild_Demented', label: 'Very Mild',    color: '#818cf8', icon: 'fa-solid fa-magnifying-glass-chart' },
      { key: 'Mild_Demented',      label: 'Mild',         color: '#c084fc', icon: 'fa-solid fa-triangle-exclamation' },
      { key: 'Moderate_Demented',  label: 'Moderate',     color: '#fb7185', icon: 'fa-solid fa-circle-exclamation' }
    ].map(s => ({
      ...s,
      count: this.dossier!.analyses.filter(a => a.prediction === s.key).length,
      pct: 0
    }))
    .map(s => ({ ...s, pct: Math.round((s.count / t) * 100) }))
    .filter(s => s.count > 0);
  }

  getTrend(i: number): string {
    if (!this.dossier || i >= this.dossier.analyses.length - 1) return '';
    const stages = ['Non_Demented','Very_Mild_Demented','Mild_Demented','Moderate_Demented'];
    const d = stages.indexOf(this.dossier.analyses[i].prediction) -
              stages.indexOf(this.dossier.analyses[i + 1].prediction);
    return d > 0 ? '↗ Progression from previous' : d < 0 ? '↙ Improvement from previous' : '→ Stable from previous';
  }

  getTrendColor(i: number): string {
    if (!this.dossier || i >= this.dossier.analyses.length - 1) return '#7c6fad';
    const stages = ['Non_Demented','Very_Mild_Demented','Mild_Demented','Moderate_Demented'];
    const d = stages.indexOf(this.dossier.analyses[i].prediction) -
              stages.indexOf(this.dossier.analyses[i + 1].prediction);
    return d > 0 ? '#fb7185' : d < 0 ? '#38bdf8' : '#818cf8';
  }

  /* ════════════════════════════════════════════════════════
     DATE FORMATTERS
  ════════════════════════════════════════════════════════ */
  formatPrediction(p: string): string { return p?.replace(/_/g, ' ') || ''; }

  shortDate(d: string): string {
    return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short' });
  }
  formatDateShort(d: string): string {
    return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: '2-digit' });
  }
  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  }
  formatDateTime(d: string): string {
    return new Date(d).toLocaleString('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}