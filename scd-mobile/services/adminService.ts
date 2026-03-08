import api from './api';

export interface DashboardResponse {
    totalPatients: number;
    totalDoctors: number;
    totalImages: number;
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
        // Returns the full URL for the backup download
        // The actual download is handled by expo-file-system + expo-sharing
        const baseUrl = api.defaults.baseURL || '';
        return `${baseUrl}/admin/backup`;
    },
};

