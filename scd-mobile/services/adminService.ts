import api from './api';

export interface DashboardResponse {
    totalPatients: number;
    totalDoctors: number;
    totalImages: number;
    totalPredictions: number;
    totalLesions: number;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

export const adminService = {
    getDashboard: async (): Promise<DashboardResponse> => {
        const response = await api.get<DashboardResponse>('/admin/dashboard');
        return response.data;
    },

    changePassword: async (data: ChangePasswordRequest): Promise<void> => {
        await api.post('/admin/change-password', data);
    },

    downloadBackup: async (): Promise<string> => {
        const baseUrl = api.defaults.baseURL || '';
        return `${baseUrl}/admin/backup`;
    },
};
