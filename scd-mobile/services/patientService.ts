import api from './api';

// ==================== Types ====================

export interface PatientRegisterRequest {
    nome: string;
    cpf: string;
    dataNascimento?: string;
    sexo: string;
    termoConsentimentoIa?: boolean;
}

export interface PatientResponse {
    id: string;
    nome: string;
    cpf: string;
    sexo: string;
    dataNascimento?: string;
    termoConsentimentoIa?: boolean;
    dataConsentimento?: string;
    criadoEm?: string;
    atualizadoEm?: string;
}

// ==================== Service ====================

export const patientService = {
    /**
     * Register a new patient (separate from consultation).
     * POST /api/patient/register
     */
    registerPatient: async (data: PatientRegisterRequest): Promise<PatientResponse> => {
        const response = await api.post<PatientResponse>('/patient/register', data);
        return response.data;
    },

    /**
     * Search for a patient by CPF.
     * GET /api/patient/search?cpf=...
     */
    searchByCpf: async (cpf: string): Promise<PatientResponse | null> => {
        try {
            const response = await api.get<PatientResponse>('/patient/search', { params: { cpf } });
            return response.data;
        } catch (err: any) {
            if (err.response?.status === 404) return null;
            throw err;
        }
    },

    /**
     * Get a patient by ID.
     * GET /api/patient/{id}
     */
    getPatient: async (id: string): Promise<PatientResponse> => {
        const response = await api.get<PatientResponse>(`/patient/${id}`);
        return response.data;
    },

    /**
     * List all patients for the logged-in doctor.
     * GET /api/patient
     */
    listPatients: async (): Promise<PatientResponse[]> => {
        const response = await api.get<PatientResponse[]>('/patient');
        return response.data;
    },
};
