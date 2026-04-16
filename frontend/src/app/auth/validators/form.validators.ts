import { Validators } from '@angular/forms';

/** Nom / prénom : uniquement lettres et espaces, pas de chiffres */
const NO_NUMBERS_PATTERN = /^[^0-9]*$/;

/** Email : format standard (déjà couvert par Validators.email) */
export const emailValidators = [Validators.required, Validators.email];

/** Mot de passe : min 6 caractères, au moins 1 majuscule, 1 chiffre, 1 caractère spécial */
const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^_\-+=[\]{}|\\:',./]).{6,}$/;
export const passwordValidators = [
  Validators.required,
  Validators.minLength(6),
  Validators.pattern(PASSWORD_PATTERN),
];

/** Mot de passe optionnel (edit profile) : vide autorisé, sinon même règle */
const PASSWORD_OPTIONAL_PATTERN = /^$|^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^_\-+=[\]{}|\\:',./]).{6,}$/;
export const passwordOptionalValidators = [
  Validators.pattern(PASSWORD_OPTIONAL_PATTERN),
];

/** Téléphone : vide autorisé, sinon exactement 8 chiffres */
const TEL_8_DIGITS = /^$|^[0-9]{8}$/;
export const numTelValidators = [Validators.pattern(TEL_8_DIGITS)];

/** Nom / prénom : requis et sans chiffres */
export const nomValidators = [
  Validators.required,
  Validators.pattern(NO_NUMBERS_PATTERN),
];
export const prenomValidators = [
  Validators.required,
  Validators.pattern(NO_NUMBERS_PATTERN),
];

/** Messages d'erreur alignés backend */
export const validationMessages: Record<string, Record<string, string>> = {
  nom: {
    required: 'Le nom est requis.',
    pattern: 'Le nom ne doit pas contenir de chiffres.',
  },
  prenom: {
    required: 'Le prénom est requis.',
    pattern: 'Le prénom ne doit pas contenir de chiffres.',
  },
  email: {
    required: "L'email est requis.",
    email: "L'email doit être au format valide (ex: utilisateur@domaine.com).",
  },
  password: {
    required: 'Le mot de passe est requis.',
    minlength: 'Le mot de passe doit contenir au moins 6 caractères.',
    pattern:
      'Le mot de passe doit contenir au moins une majuscule, un chiffre et un caractère spécial.',
  },
  numTel: {
    pattern: 'Le numéro de téléphone doit contenir exactement 8 chiffres.',
  },
};

export function getControlErrorMessage(controlName: string, errors: Record<string, unknown> | null): string {
  if (!errors) return '';
  const messages = validationMessages[controlName];
  if (!messages) return '';
  if (errors['required']) return messages['required'] ?? '';
  if (errors['email']) return messages['email'] ?? '';
  if (errors['minlength']) return messages['minlength'] ?? '';
  if (errors['pattern']) return messages['pattern'] ?? '';
  return '';
}
