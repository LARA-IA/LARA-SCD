import api from './api';

export interface PatientRegisterRequest {
    nome: string;
    cpf: string;
    dataNascimento?: string; // ISO date string, e.g. "2000-01-15"
    sexo: string;
}

export interface PatientImageResponse {
    id: string;
    fileName: string;
    filePath: string;
    fileSize: number;
    contentType: string;
    localizacao: string;
    aiDiagnosis?: string;
    confidence?: number;
    multClass?: string;
    multClassConfidence?: number;
    doctorVerdict?: string;
    confirmed: boolean;
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
 * @deprecated Use consultationService instead for the new consultation-based flow.
 */
export const predictService = {
    registerPatient: async (data: PatientRegisterRequest): Promise<any> => {
        const response = await api.post('/patient/register', data);
        return response.data;
    },

    classifyImage: async (
        patientId: string,
        imageUri: string,
        imageName: string,
        imageType: string,
        localizacao: string
    ): Promise<PatientImageResponse> => {
        const formData = new FormData();
        formData.append('file', {
            uri: imageUri,
            name: imageName,
            type: imageType,
        } as any);
        formData.append('localizacao', localizacao);

        const response = await api.post<PatientImageResponse>(
            `/predict/classify/${patientId}`,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
                timeout: 60000,
            }
        );
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

