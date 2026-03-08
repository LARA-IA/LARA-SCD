import api from './api';

// ==================== Types ====================

export interface ConsultationRequest {
    nome: string;
    cpf: string;
    sexo: string;
    dataNascimento?: string;
    localizacao: string;
    images: { uri: string; name: string; type: string }[];
}

export interface ConsultationResponse {
    id: string;
    patient: {
        id: string;
        nome: string;
        cpf: string;
        sexo: string;
    };
    doctor: {
        id: string;
        nome: string;
    };
    aiDiagnosis?: string;
    confidence?: number;
    multClass?: string;
    multClassConfidence?: number;
    finalDiagnosis?: string;
    confirmed: boolean;
    createdAt: string;
    images: ImageInfo[];
}

export interface ImageInfo {
    id: string;
    filePath: string;
    fileName: string;
    fileSize: number;
    contentType: string;
    aiDiagnosis?: string;
    confidence?: number;
    multClass?: string;
    multClassConfidence?: number;
    finalDiagnosis?: string;
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

// ==================== Service ====================

export const consultationService = {
    /**
     * Creates a new consultation with patient data and images.
     * POST /api/medico/consultations (multipart/form-data)
     */
    createConsultation: async (data: ConsultationRequest): Promise<ConsultationResponse> => {
        const formData = new FormData();
        formData.append('nome', data.nome);
        formData.append('cpf', data.cpf);
        formData.append('sexo', data.sexo);
        formData.append('localizacao', data.localizacao);
        if (data.dataNascimento) {
            formData.append('dataNascimento', data.dataNascimento);
        }

        data.images.forEach((img) => {
            formData.append('images', {
                uri: img.uri,
                name: img.name,
                type: img.type,
            } as any);
        });

        const response = await api.post<ConsultationResponse>(
            '/medico/consultations',
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
                timeout: 120000, // 2 min timeout for AI + multiple images
            }
        );
        return response.data;
    },

    /**
     * List consultations for the logged-in doctor.
     * GET /api/medico/consultations
     */
    listConsultations: async (nome?: string, cpf?: string): Promise<ConsultationResponse[]> => {
        const params: any = {};
        if (nome) params.nome = nome;
        if (cpf) params.cpf = cpf;
        const response = await api.get<ConsultationResponse[]>('/medico/consultations', { params });
        return response.data;
    },

    /**
     * Get a single consultation by ID.
     * GET /api/medico/consultations/{id}
     */
    getConsultation: async (id: string): Promise<ConsultationResponse> => {
        const response = await api.get<ConsultationResponse>(`/medico/consultations/${id}`);
        return response.data;
    },

    /**
     * Confirm the diagnosis at the consultation level.
     * PUT /api/medico/consultations/{id}/confirm
     */
    confirmConsultationDiagnosis: async (
        consultationId: string,
        finalDiagnosis: DoctorVerdict
    ): Promise<ConsultationResponse> => {
        const response = await api.put<ConsultationResponse>(
            `/medico/consultations/${consultationId}/confirm`,
            { finalDiagnosis }
        );
        return response.data;
    },

    /**
     * Confirm the diagnosis for an individual image.
     * PUT /api/medico/consultations/images/{imageId}/confirm
     */
    confirmImageDiagnosis: async (
        imageId: string,
        finalDiagnosis: DoctorVerdict
    ): Promise<ConsultationResponse> => {
        const response = await api.put<ConsultationResponse>(
            `/medico/consultations/images/${imageId}/confirm`,
            { finalDiagnosis }
        );
        return response.data;
    },
};
