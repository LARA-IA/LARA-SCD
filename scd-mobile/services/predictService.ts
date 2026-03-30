import api from './api';

export interface PatientRegisterRequest {
    nome: string;
    cpf: string;
    dataNascimento?: string;
    sexo: string;
    termoConsentimentoIa?: boolean;
}

export interface PatientImageResponse {
    id: string;
    fileName: string;
    filePath: string;
    fileSize: number;
    contentType: string;
    localizacao: string;
    doctorVerdict?: string;
    confirmed: boolean;
    concordanciaIa?: boolean;
    statusProcessamentoIa?: string;
    predictions: {
        id: string;
        versaoModelo: string;
        classeInferida?: string;
        confianca?: number;
        multClasse?: string;
        confiancaMultClasse?: number;
        criadoEm?: string;
    }[];
}

export type DoctorVerdict =
    | 'AGUARDANDO_BIOPSIA'
    | 'MELANOMA'
    | 'CARCINOMA_BASOCELULAR'
    | 'CARCINOMA_ESPINOCELULAR'
    | 'QUERATOSE_ACTINICA'
    | 'NEVO';

export const DoctorVerdictLabels: Record<DoctorVerdict, string> = {
    AGUARDANDO_BIOPSIA: 'Aguardando Biópsia',
    MELANOMA: 'Melanoma',
    CARCINOMA_BASOCELULAR: 'Carcinoma Basocelular',
    CARCINOMA_ESPINOCELULAR: 'Carcinoma Espinocelular',
    QUERATOSE_ACTINICA: 'Queratose Actínica',
    NEVO: 'Nevo',
};

export const DoctorVerdictOptions = Object.entries(DoctorVerdictLabels).map(([value, label]) => ({
    value: value as DoctorVerdict,
    label,
}));

/**
 * @deprecated Use patientService + consultationService instead for the new separated flow.
 */
export const predictService = {
    registerPatient: async (data: PatientRegisterRequest): Promise<any> => {
        const response = await api.post('/patient/register', data);
        return response.data;
    },

    confirmDiagnosis: async (
        imageId: string,
        verdict: DoctorVerdict
    ): Promise<PatientImageResponse> => {
        const response = await api.put<PatientImageResponse>(
            `/medico/consultations/images/${imageId}/confirm`,
            { finalDiagnosis: verdict }
        );
        return response.data;
    },
};
