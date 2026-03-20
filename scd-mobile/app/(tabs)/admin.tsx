import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    RefreshControl,
    ActivityIndicator,
    Alert,
    Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Paths, File as ExpoFile } from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { adminService, DashboardResponse } from '../../services/adminService';

export default function AdminDashboardScreen() {
    const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState('');
    const [downloadingBackup, setDownloadingBackup] = useState(false);

    useEffect(() => {
        loadDashboard();
    }, []);

    const loadDashboard = async () => {
        try {
            setError('');
            const data = await adminService.getDashboard();
            setDashboard(data);
        } catch (err: any) {
            setError('Erro ao carregar dashboard');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    const handleRefresh = () => {
        setRefreshing(true);
        loadDashboard();
    };

    const handleDownloadBackup = async () => {
        setDownloadingBackup(true);
        try {
            const token = await AsyncStorage.getItem('token');
            const backupUrl = await adminService.downloadBackup();
            const dateStr = new Date().toISOString().split('T')[0];

            const response = await fetch(backupUrl, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                setError('Erro ao baixar backup');
                return;
            }

            const blob = await response.blob();
            const fileName = `scd_backup_${dateStr}.zip`;

            if (Platform.OS === 'web') {
                // Web: trigger browser download
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(url);
                Alert.alert('Sucesso', 'Backup baixado com sucesso!');
            } else {
                // Native: use expo-file-system + expo-sharing
                const file = new ExpoFile(Paths.cache, fileName);
                const arrayBuffer = await blob.arrayBuffer();
                const uint8 = new Uint8Array(arrayBuffer);
                const writer = file.writableStream().getWriter();
                await writer.write(uint8);
                await writer.close();

                const canShare = await Sharing.isAvailableAsync();
                if (canShare) {
                    await Sharing.shareAsync(file.uri, {
                        mimeType: 'application/zip',
                        dialogTitle: 'Salvar Backup SCD',
                    });
                } else {
                    Alert.alert('Sucesso', 'Backup salvo no cache do app.');
                }
            }
        } catch (err: any) {
            setError('Erro ao baixar backup');
        } finally {
            setDownloadingBackup(false);
        }
    };

    if (loading) {
        return (
            <View style={styles.loadingContainer}>
                <ActivityIndicator size="large" color="#06b6d4" />
                <Text style={styles.loadingText}>Carregando dashboard...</Text>
            </View>
        );
    }

    return (
        <ScrollView
            style={styles.container}
            contentContainerStyle={styles.content}
            refreshControl={
                <RefreshControl
                    refreshing={refreshing}
                    onRefresh={handleRefresh}
                    tintColor="#06b6d4"
                    colors={['#06b6d4']}
                />
            }
        >
            {error ? (
                <View style={styles.errorContainer}>
                    <Ionicons name="alert-circle" size={18} color="#ef4444" />
                    <Text style={styles.errorText}>{error}</Text>
                    <TouchableOpacity onPress={() => setError('')}>
                        <Ionicons name="close" size={18} color="#ef4444" />
                    </TouchableOpacity>
                </View>
            ) : null}

            {/* Stats Cards */}
            <Text style={styles.sectionTitle}>Estatísticas</Text>
            <View style={styles.statsGrid}>
                <View style={[styles.statCard, { borderLeftColor: '#06b6d4' }]}>
                    <View style={styles.statIconRow}>
                        <View style={[styles.statIconBg, { backgroundColor: 'rgba(6, 182, 212, 0.15)' }]}>
                            <Ionicons name="people" size={24} color="#06b6d4" />
                        </View>
                    </View>
                    <Text style={styles.statNumber}>{dashboard?.totalPatients ?? 0}</Text>
                    <Text style={styles.statLabel}>Total de Pacientes</Text>
                </View>

                <View style={[styles.statCard, { borderLeftColor: '#8b5cf6' }]}>
                    <View style={styles.statIconRow}>
                        <View style={[styles.statIconBg, { backgroundColor: 'rgba(139, 92, 246, 0.15)' }]}>
                            <Ionicons name="medical" size={24} color="#8b5cf6" />
                        </View>
                    </View>
                    <Text style={styles.statNumber}>{dashboard?.totalDoctors ?? 0}</Text>
                    <Text style={styles.statLabel}>Total de Médicos</Text>
                </View>

                <View style={[styles.statCard, { borderLeftColor: '#f59e0b' }]}>
                    <View style={styles.statIconRow}>
                        <View style={[styles.statIconBg, { backgroundColor: 'rgba(245, 158, 11, 0.15)' }]}>
                            <Ionicons name="images" size={24} color="#f59e0b" />
                        </View>
                    </View>
                    <Text style={styles.statNumber}>{dashboard?.totalImages ?? 0}</Text>
                    <Text style={styles.statLabel}>Total de Imagens</Text>
                </View>
            </View>

            {/* Actions */}
            <Text style={styles.sectionTitle}>Ações</Text>
            <TouchableOpacity
                style={styles.actionButton}
                onPress={handleDownloadBackup}
                disabled={downloadingBackup}
                activeOpacity={0.8}
            >
                {downloadingBackup ? (
                    <ActivityIndicator color="#fff" size="small" />
                ) : (
                    <Ionicons name="download-outline" size={22} color="#fff" />
                )}
                <Text style={styles.actionButtonText}>
                    {downloadingBackup ? 'Baixando...' : 'Download Backup'}
                </Text>
            </TouchableOpacity>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0f172a',
    },
    content: {
        padding: 20,
        paddingBottom: 40,
    },
    loadingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#0f172a',
        gap: 12,
    },
    loadingText: {
        color: '#94a3b8',
        fontSize: 14,
    },
    errorContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(239, 68, 68, 0.15)',
        padding: 12,
        borderRadius: 12,
        marginBottom: 16,
        gap: 8,
    },
    errorText: {
        color: '#ef4444',
        fontSize: 13,
        flex: 1,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#f1f5f9',
        marginBottom: 16,
        marginTop: 8,
    },
    statsGrid: {
        gap: 12,
        marginBottom: 24,
    },
    statCard: {
        backgroundColor: '#1e293b',
        borderRadius: 16,
        padding: 20,
        borderLeftWidth: 4,
        borderWidth: 1,
        borderColor: '#334155',
    },
    statIconRow: {
        marginBottom: 12,
    },
    statIconBg: {
        width: 44,
        height: 44,
        borderRadius: 12,
        justifyContent: 'center',
        alignItems: 'center',
    },
    statNumber: {
        fontSize: 36,
        fontWeight: '800',
        color: '#f1f5f9',
        marginBottom: 4,
    },
    statLabel: {
        fontSize: 14,
        color: '#94a3b8',
        fontWeight: '500',
    },
    actionButton: {
        backgroundColor: '#06b6d4',
        borderRadius: 14,
        height: 52,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 10,
        shadowColor: '#06b6d4',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 6,
    },
    actionButtonText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: '700',
    },
});
