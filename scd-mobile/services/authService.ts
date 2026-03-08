import api from './api';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    type: string;
    id: string;
    nome: string;
    email: string;
    accessLevel: 'MANAGER' | 'DOCTOR';
}

export interface DoctorRegisterRequest {
    nome: string;
    cpf: string;
    email: string;
    password: string;
    CRM: string;
}

export const authService = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post<LoginResponse>('/auth/login', credentials);
        return response.data;
    },

    registerDoctor: async (data: DoctorRegisterRequest): Promise<void> => {
        await api.post('/auth/register', data);
    },
};

