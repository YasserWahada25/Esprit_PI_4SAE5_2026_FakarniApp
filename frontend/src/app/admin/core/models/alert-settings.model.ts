// core/models/alert-settings.model.ts

export interface AlertSettings {
    emailEnabled:  boolean;
    emailSubject:  string;
    emailTemplate: string;
}

export const DEFAULT_ALERT_SETTINGS: AlertSettings = {
    emailEnabled:  true,
    emailSubject:  '🚨 ALERTE - Patient {{patientId}} hors zone',
    emailTemplate: `<h2>⚠️ Alerte de Géolocalisation</h2>
<p>Patient <strong>{{patientId}}</strong> a déclenché une alerte.</p>
<p><b>Type :</b> {{type}}</p>
<p><b>Distance hors zone :</b> {{distance}} mètres</p>
<p><b>Heure :</b> {{timestamp}}</p>
<p>Veuillez vérifier immédiatement la localisation du patient.</p>`
};