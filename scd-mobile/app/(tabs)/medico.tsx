import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    ActivityIndicator,
    Alert,
    Image,
    Modal,
    FlatList,
    RefreshControl,
    Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import {
    consultationService,
    ConsultationResponse,
    ImageInfo,
    DoctorVerdict,
    DoctorVerdictLabels,
    DoctorVerdictOptions,
    Localizacao,
    LocalizacaoLabels,
    LocalizacaoOptions,
} from '../../services/consultationService';

// Helper: get API base URL (mirrors api.ts)
const getBaseUrl = () => {
    if (Platform.OS === 'android') {
        return 'http://192.168.1.4:8080/api';
    }
    return 'http://127.0.0.1:8080/api';
};

const getImageUrl = (filePath: string) => {
    if (!filePath) return '';
    const filename = filePath.split(/[/\\]/).pop() || filePath;
    const baseUrl = getBaseUrl().replace('/api', '');
    return `${baseUrl}/api/files/by-name/${encodeURIComponent(filename)}`;
};

const formatDate = (dateStr: string) => {
    try {
        const d = new Date(dateStr);
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        return `${day}/${month}/${year} ${hours}:${minutes}`;
    } catch {
        return dateStr;
    }
};

interface PatientFormData {
    nome: string;
    cpf: string;
    dataNascimento: string;
    sexo: 'M' | 'F';
}

export default function MedicoDashboardScreen() {
    // ==================== State ====================
    const [consultations, setConsultations] = useState<ConsultationResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState('');

    // Filters
    const [filterNome, setFilterNome] = useState('');
    const [filterCpf, setFilterCpf] = useState('');

    // New consultation modal
    const [newConsultationOpen, setNewConsultationOpen] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [patientData, setPatientData] = useState<PatientFormData>({
        nome: '', cpf: '', dataNascimento: '', sexo: 'M',
    });
    const [images, setImages] = useState<{ uri: string; name: string; type: string }[]>([]);
    const [imageLocalizacoes, setImageLocalizacoes] = useState<string[]>([]);
    const [isExistingPatient, setIsExistingPatient] = useState(false);
    const [showSexPicker, setShowSexPicker] = useState(false);
    const [showLocPicker, setShowLocPicker] = useState(false);
    const [locPickerIndex, setLocPickerIndex] = useState<number>(-1);
    const [showDatePicker, setShowDatePicker] = useState(false);
    const [datePickerMonth, setDatePickerMonth] = useState(new Date().getMonth());
    const [datePickerYear, setDatePickerYear] = useState(new Date().getFullYear());

    // Detail modal
    const [detailModalOpen, setDetailModalOpen] = useState(false);
    const [selectedConsultation, setSelectedConsultation] = useState<ConsultationResponse | null>(null);
    const [loadingDetail, setLoadingDetail] = useState(false);

    // Per-image verdict picker
    const [verdictPickerImageId, setVerdictPickerImageId] = useState<string | null>(null);

    interface GroupedPatient {
        patient: { id: string; nome: string; cpf: string; sexo: string; dataNascimento?: string };
        consultations: ConsultationResponse[];
    }

    const [patientConsultationsModalOpen, setPatientConsultationsModalOpen] = useState(false);
    const [selectedPatient, setSelectedPatient] = useState<GroupedPatient | null>(null);

    const groupedPatients: GroupedPatient[] = useMemo(() => {
        const filtered = consultations.filter(c => {
            const matchNome = !filterNome || c.patient.nome.toLowerCase().includes(filterNome.toLowerCase());
            const matchCpf = !filterCpf || c.patient.cpf.includes(filterCpf);
            return matchNome && matchCpf;
        });
        const map = new Map<string, GroupedPatient>();
        for (const c of filtered) {
            if (!map.has(c.patient.id)) {
                map.set(c.patient.id, { patient: c.patient, consultations: [] });
            }
            map.get(c.patient.id)!.consultations.push(c);
        }
        return Array.from(map.values());
    }, [consultations, filterNome, filterCpf]);

    const localizacaoList = LocalizacaoOptions;

    // ==================== Load Consultations ====================
    const loadConsultations = useCallback(async (nome?: string, cpf?: string) => {
        try {
            setLoading(true);
            const data = await consultationService.listConsultations(nome, cpf);
            setConsultations(data);
        } catch (err: any) {
            setError('Erro ao carregar consultas');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, []);

    useEffect(() => {
        loadConsultations();
    }, [loadConsultations]);

    const handleFilter = () => {
        loadConsultations(
            filterNome.trim() || undefined,
            filterCpf.trim() || undefined,
        );
    };

    const handleClearFilters = () => {
        setFilterNome('');
        setFilterCpf('');
        loadConsultations();
    };

    const onRefresh = () => {
        setRefreshing(true);
        loadConsultations(
            filterNome.trim() || undefined,
            filterCpf.trim() || undefined,
        );
    };

    // ==================== New Consultation ====================
    const pickImage = async () => {
        const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
        if (status !== 'granted') {
            Alert.alert('Permissão necessária', 'Precisamos de acesso à galeria.');
            return;
        }
        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ['images'],
            allowsEditing: true,
            quality: 0.8,
        });
        if (!result.canceled && result.assets[0]) {
            const asset = result.assets[0];
            setImages(prev => [...prev, {
                uri: asset.uri,
                name: asset.fileName || `image_${Date.now()}.jpg`,
                type: asset.mimeType || 'image/jpeg',
            }]);
            setImageLocalizacoes(prev => [...prev, '']);
        }
    };

    const takePhoto = async () => {
        const { status } = await ImagePicker.requestCameraPermissionsAsync();
        if (status !== 'granted') {
            Alert.alert('Permissão necessária', 'Precisamos de acesso à câmera.');
            return;
        }
        const result = await ImagePicker.launchCameraAsync({
            allowsEditing: true,
            quality: 0.8,
        });
        if (!result.canceled && result.assets[0]) {
            const asset = result.assets[0];
            setImages(prev => [...prev, {
                uri: asset.uri,
                name: asset.fileName || `photo_${Date.now()}.jpg`,
                type: asset.mimeType || 'image/jpeg',
            }]);
            setImageLocalizacoes(prev => [...prev, '']);
        }
    };

    const removeImage = (index: number) => {
        setImages(prev => prev.filter((_, i) => i !== index));
        setImageLocalizacoes(prev => prev.filter((_, i) => i !== index));
    };

    const handleCreateConsultation = async () => {
        if (!patientData.nome || !patientData.cpf) {
            setError('Nome e CPF do paciente são obrigatórios');
            return;
        }
        if (images.length === 0) {
            setError('Selecione pelo menos uma imagem');
            return;
        }
        if (images.length > 20) {
            setError('Máximo de 20 imagens por consulta');
            return;
        }
        const allLocsFilled = imageLocalizacoes.length === images.length && imageLocalizacoes.every(l => !!l);
        if (!allLocsFilled) {
            setError('Informe a localização de cada imagem');
            return;
        }

        setError('');
        setUploading(true);

        try {
            const newConsultation = await consultationService.createConsultation({
                nome: patientData.nome,
                cpf: patientData.cpf,
                sexo: patientData.sexo,
                dataNascimento: patientData.dataNascimento || undefined,
                localizacoes: imageLocalizacoes,
                images,
            });
            setConsultations(prev => [newConsultation, ...prev]);
            setNewConsultationOpen(false);
            resetNewConsultationForm();
        } catch (err: any) {
            setError(err.response?.data?.error || err.response?.data?.detail || 'Erro ao criar consulta');
        } finally {
            setUploading(false);
        }
    };

    const resetNewConsultationForm = () => {
        setPatientData({ nome: '', cpf: '', dataNascimento: '', sexo: 'M' });
        setImages([]);
        setImageLocalizacoes([]);
        setIsExistingPatient(false);
    };

    const startNewConsultationForPatient = (consultation: ConsultationResponse) => {
        const p = consultation.patient;
        setPatientData({
            nome: p.nome,
            cpf: p.cpf,
            dataNascimento: p.dataNascimento || '',
            sexo: (p.sexo as 'M' | 'F') || 'M',
        });
        setImages([]);
        setImageLocalizacoes([]);
        setIsExistingPatient(true);
        setDetailModalOpen(false);
        setSelectedConsultation(null);
        setNewConsultationOpen(true);
    };

    // ==================== Detail Modal ====================
    const handleOpenDetailModal = async (consultationId: string) => {
        try {
            setError('');
            setLoadingDetail(true);
            const consultation = await consultationService.getConsultation(consultationId);
            setSelectedConsultation(consultation);
            setDetailModalOpen(true);
        } catch (err: any) {
            setError(err.response?.data?.error || 'Erro ao carregar detalhes da consulta');
        } finally {
            setLoadingDetail(false);
        }
    };

    // ==================== Per-Image Diagnosis Confirmation ====================
    const handleConfirmImageDiagnosis = async (imageId: string, verdict: DoctorVerdict) => {
        if (!selectedConsultation) return;

        try {
            const updatedConsultation = await consultationService.confirmImageDiagnosis(imageId, verdict);
            setSelectedConsultation(updatedConsultation);
            // Update in the list too
            setConsultations(prev =>
                prev.map(c => c.id === updatedConsultation.id ? updatedConsultation : c)
            );
            setVerdictPickerImageId(null);
        } catch (err: any) {
            setError(err.response?.data?.error || 'Erro ao confirmar diagnóstico');
        }
    };

    // ==================== Patient Row ====================
    const renderPatientItem = ({ item }: { item: GroupedPatient }) => {
        return (
            <TouchableOpacity
                style={styles.consultationCard}
                onPress={() => {
                    setSelectedPatient(item);
                    setPatientConsultationsModalOpen(true);
                }}
                activeOpacity={0.7}
            >
                <View style={styles.consultationCardHeader}>
                    <View style={{ flex: 1 }}>
                        <Text style={styles.patientName}>{item.patient.nome}</Text>
                        <Text style={styles.patientCpf}>CPF: {item.patient.cpf}</Text>
                    </View>
                    <View style={[styles.statusBadge, { backgroundColor: '#3b82f625' }]}>
                        <View style={[styles.statusDot, { backgroundColor: '#3b82f6' }]} />
                        <Text style={[styles.statusText, { color: '#3b82f6' }]}>{item.consultations.length} consulta(s)</Text>
                    </View>
                </View>

                <View style={styles.consultationCardFooter}>
                    <View style={{ flex: 1 }} />
                    <TouchableOpacity
                        style={{ flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: 'rgba(6, 182, 212, 0.15)', borderRadius: 8, paddingHorizontal: 10, paddingVertical: 4 }}
                        onPress={(e) => { e.stopPropagation(); startNewConsultationForPatient(item.consultations[0]); }}
                        activeOpacity={0.7}
                    >
                        <Ionicons name="add-circle-outline" size={14} color="#06b6d4" />
                        <Text style={{ color: '#06b6d4', fontSize: 12, fontWeight: '600' }}>Nova Consulta</Text>
                    </TouchableOpacity>
                    <Ionicons name="chevron-forward" size={18} color="#475569" style={{ marginLeft: 12 }} />
                </View>
            </TouchableOpacity>
        );
    };

    // ==================== Consultation Row ====================
    const getConfirmationStatus = (consultation: ConsultationResponse) => {
        const imgs = consultation.images || [];
        if (imgs.length === 0) return { label: 'Sem imagens', color: '#64748b' };
        const confirmed = imgs.filter(img => img.confirmed).length;
        if (confirmed === imgs.length) return { label: 'Todas confirmadas', color: '#22c55e' };
        return { label: `${confirmed}/${imgs.length} confirmadas`, color: '#f59e0b' };
    };

    const renderConsultationItem = ({ item }: { item: ConsultationResponse }) => {
        const status = getConfirmationStatus(item);
        return (
            <TouchableOpacity
                style={styles.consultationCard}
                onPress={() => handleOpenDetailModal(item.id)}
                activeOpacity={0.7}
            >
                <View style={styles.consultationCardHeader}>
                    <View style={{ flex: 1 }}>
                        <Text style={styles.patientName}>{formatDate(item.createdAt)}</Text>
                        <Text style={styles.patientCpf}>{item.images?.length || 0} imagem(ns)</Text>
                    </View>
                    <View style={[styles.statusBadge, { backgroundColor: status.color + '25' }]}>
                        <View style={[styles.statusDot, { backgroundColor: status.color }]} />
                        <Text style={[styles.statusText, { color: status.color }]}>{status.label}</Text>
                    </View>
                </View>

                <View style={styles.consultationCardFooter}>
                    <View style={{ flex: 1 }} />
                    <Ionicons name="chevron-forward" size={18} color="#475569" />
                </View>
            </TouchableOpacity>
        );
    };

    // ==================== RENDER ====================
    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={styles.headerTitle}>Minhas Consultas</Text>
                <TouchableOpacity
                    style={styles.newButton}
                    onPress={() => setNewConsultationOpen(true)}
                    activeOpacity={0.8}
                >
                    <Ionicons name="add" size={20} color="#fff" />
                    <Text style={styles.newButtonText}>Nova Consulta</Text>
                </TouchableOpacity>
            </View>

            {error ? (
                <View style={styles.errorContainer}>
                    <Ionicons name="alert-circle" size={18} color="#ef4444" />
                    <Text style={styles.errorText}>{error}</Text>
                    <TouchableOpacity onPress={() => setError('')}>
                        <Ionicons name="close" size={18} color="#ef4444" />
                    </TouchableOpacity>
                </View>
            ) : null}

            {/* Filters */}
            <View style={styles.filterSection}>
                <View style={styles.filterRow}>
                    <View style={styles.filterInput}>
                        <Ionicons name="search-outline" size={16} color="#64748b" />
                        <TextInput
                            style={styles.filterTextInput}
                            placeholder="Nome do paciente"
                            placeholderTextColor="#64748b"
                            value={filterNome}
                            onChangeText={setFilterNome}
                            onSubmitEditing={handleFilter}
                        />
                    </View>
                    <View style={styles.filterInput}>
                        <Ionicons name="card-outline" size={16} color="#64748b" />
                        <TextInput
                            style={styles.filterTextInput}
                            placeholder="CPF"
                            placeholderTextColor="#64748b"
                            value={filterCpf}
                            onChangeText={setFilterCpf}
                            keyboardType="numeric"
                            onSubmitEditing={handleFilter}
                        />
                    </View>
                </View>
                <View style={styles.filterButtons}>
                    <TouchableOpacity style={styles.filterBtn} onPress={handleFilter}>
                        <Ionicons name="funnel-outline" size={16} color="#06b6d4" />
                        <Text style={styles.filterBtnText}>Filtrar</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.clearBtn} onPress={handleClearFilters}>
                        <Text style={styles.clearBtnText}>Limpar</Text>
                    </TouchableOpacity>
                </View>
            </View>

            {/* Consultations List */}
            {loading && consultations.length === 0 ? (
                <View style={styles.centerContainer}>
                    <ActivityIndicator size="large" color="#06b6d4" />
                    <Text style={styles.loadingText}>Carregando pacientes...</Text>
                </View>
            ) : (
                <FlatList
                    data={groupedPatients}
                    keyExtractor={(item) => item.patient.id}
                    renderItem={renderPatientItem}
                    contentContainerStyle={styles.listContent}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor="#06b6d4"
                            colors={['#06b6d4']}
                        />
                    }
                    ListEmptyComponent={
                        <View style={styles.emptyContainer}>
                            <Ionicons name="document-text-outline" size={48} color="#334155" />
                            <Text style={styles.emptyText}>Nenhum paciente encontrado</Text>
                            <Text style={styles.emptySubText}>Crie uma nova consulta para começar</Text>
                        </View>
                    }
                />
            )}

            {/* Loading detail indicator */}
            {loadingDetail && (
                <View style={styles.loadingOverlay}>
                    <ActivityIndicator size="large" color="#06b6d4" />
                </View>
            )}

            {/* ==================== NEW CONSULTATION MODAL ==================== */}
            <Modal visible={newConsultationOpen} animationType="slide" presentationStyle="pageSheet">
                <View style={styles.modalContainer}>
                    <View style={styles.modalHeader}>
                        <Text style={styles.modalHeaderTitle}>
                            {isExistingPatient ? `Nova Consulta - ${patientData.nome}` : 'Nova Consulta'}
                        </Text>
                        <TouchableOpacity onPress={() => { setNewConsultationOpen(false); resetNewConsultationForm(); }}>
                            <Ionicons name="close" size={24} color="#94a3b8" />
                        </TouchableOpacity>
                    </View>

                    <ScrollView style={styles.modalBody} contentContainerStyle={{ paddingBottom: 40 }} keyboardShouldPersistTaps="handled">
                        {/* Patient Data — only for NEW patients */}
                        {isExistingPatient ? (
                            <View style={[styles.formSection, { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12, paddingHorizontal: 16, backgroundColor: 'rgba(6, 182, 212, 0.08)', borderRadius: 12, borderWidth: 1, borderColor: 'rgba(6, 182, 212, 0.2)' }]}>
                                <Ionicons name="person-circle-outline" size={32} color="#06b6d4" />
                                <View style={{ flex: 1 }}>
                                    <Text style={{ color: '#f1f5f9', fontSize: 16, fontWeight: '700' }}>{patientData.nome}</Text>
                                    <Text style={{ color: '#94a3b8', fontSize: 13, marginTop: 2 }}>CPF: {patientData.cpf}</Text>
                                </View>
                            </View>
                        ) : (
                            <View style={styles.formSection}>
                                <View style={styles.sectionHeader}>
                                    <Ionicons name="person-outline" size={20} color="#06b6d4" />
                                    <Text style={styles.sectionTitle}>Dados do Paciente</Text>
                                </View>

                                <View style={styles.inputGroup}>
                                    <Text style={styles.label}>Nome Completo *</Text>
                                    <View style={styles.inputContainer}>
                                        <TextInput
                                            style={styles.input}
                                            placeholder="Nome do paciente"
                                            placeholderTextColor="#64748b"
                                            value={patientData.nome}
                                            onChangeText={(v) => setPatientData({ ...patientData, nome: v })}
                                        />
                                    </View>
                                </View>

                                <View style={styles.inputGroup}>
                                    <Text style={styles.label}>CPF *</Text>
                                    <View style={styles.inputContainer}>
                                        <TextInput
                                            style={styles.input}
                                            placeholder="000.000.000-00"
                                            placeholderTextColor="#64748b"
                                            value={patientData.cpf}
                                            onChangeText={(v) => setPatientData({ ...patientData, cpf: v })}
                                            keyboardType="numeric"
                                        />
                                    </View>
                                </View>

                                <View style={styles.row}>
                                    <View style={[styles.inputGroup, { flex: 1, marginRight: 8 }]}>
                                        <Text style={styles.label}>Sexo *</Text>
                                        <TouchableOpacity
                                            style={styles.pickerButton}
                                            onPress={() => setShowSexPicker(true)}
                                        >
                                            <Text style={styles.pickerButtonText}>
                                                {patientData.sexo === 'M' ? 'Masculino' : 'Feminino'}
                                            </Text>
                                            <Ionicons name="chevron-down" size={18} color="#64748b" />
                                        </TouchableOpacity>
                                    </View>
                                    <View style={[styles.inputGroup, { flex: 1, marginLeft: 8 }]}>
                                        <Text style={styles.label}>Data Nasc.</Text>
                                        <TouchableOpacity
                                            style={styles.pickerButton}
                                            onPress={() => {
                                                if (patientData.dataNascimento) {
                                                    const parts = patientData.dataNascimento.split('-');
                                                    setDatePickerYear(parseInt(parts[0]));
                                                    setDatePickerMonth(parseInt(parts[1]) - 1);
                                                }
                                                setShowDatePicker(true);
                                            }}
                                        >
                                            <Text style={[styles.pickerButtonText, !patientData.dataNascimento && { color: '#64748b' }]}>
                                                {patientData.dataNascimento
                                                    ? patientData.dataNascimento.split('-').reverse().join('/')
                                                    : 'Selecionar'}
                                            </Text>
                                            <Ionicons name="calendar-outline" size={18} color="#64748b" />
                                        </TouchableOpacity>
                                    </View>
                                </View>
                            </View>
                        )}

                        {/* Images */}
                        <View style={styles.formSection}>
                            <View style={styles.sectionHeader}>
                                <Ionicons name="camera-outline" size={20} color="#06b6d4" />
                                <Text style={styles.sectionTitle}>Imagens da Lesão</Text>
                            </View>

                            {images.length > 0 ? (
                                <View>
                                    {images.map((img, index) => (
                                        <View key={index} style={styles.imagePreviewContainer}>
                                            <Image source={{ uri: img.uri }} style={styles.imagePreview} resizeMode="contain" />
                                            <TouchableOpacity
                                                style={styles.removeImageBtn}
                                                onPress={() => removeImage(index)}
                                            >
                                                <Ionicons name="close-circle" size={28} color="#ef4444" />
                                            </TouchableOpacity>
                                            {/* Per-image localizacao picker */}
                                            <TouchableOpacity
                                                style={[styles.pickerButton, { marginTop: 8 }]}
                                                onPress={() => { setLocPickerIndex(index); setShowLocPicker(true); }}
                                            >
                                                <Ionicons name="location-outline" size={16} color="#06b6d4" />
                                                <Text style={[styles.pickerButtonText, !imageLocalizacoes[index] && { color: '#64748b' }]}>
                                                    {imageLocalizacoes[index] ? LocalizacaoLabels[imageLocalizacoes[index] as Localizacao] : 'Localização da lesão'}
                                                </Text>
                                                <Ionicons name="chevron-down" size={18} color="#64748b" />
                                            </TouchableOpacity>
                                        </View>
                                    ))}
                                </View>
                            ) : (
                                <View style={styles.imagePlaceholder}>
                                    <Ionicons name="image-outline" size={48} color="#475569" />
                                    <Text style={styles.imagePlaceholderText}>Selecione ou tire fotos da lesão</Text>
                                </View>
                            )}

                            <View style={styles.imageButtons}>
                                <TouchableOpacity style={styles.imageButton} onPress={pickImage} activeOpacity={0.8}>
                                    <Ionicons name="images-outline" size={22} color="#06b6d4" />
                                    <Text style={styles.imageButtonText}>Galeria</Text>
                                </TouchableOpacity>
                                <TouchableOpacity style={styles.imageButton} onPress={takePhoto} activeOpacity={0.8}>
                                    <Ionicons name="camera-outline" size={22} color="#06b6d4" />
                                    <Text style={styles.imageButtonText}>Câmera</Text>
                                </TouchableOpacity>
                            </View>
                        </View>

                        {/* Create Button */}
                        <TouchableOpacity
                            style={[
                                styles.createButton,
                                (uploading || images.length === 0 || !patientData.nome || !patientData.cpf || imageLocalizacoes.length !== images.length || imageLocalizacoes.some(l => !l)) && styles.buttonDisabled,
                            ]}
                            onPress={handleCreateConsultation}
                            disabled={uploading || images.length === 0 || !patientData.nome || !patientData.cpf || imageLocalizacoes.length !== images.length || imageLocalizacoes.some(l => !l)}
                            activeOpacity={0.8}
                        >
                            {uploading ? (
                                <ActivityIndicator color="#fff" size="small" />
                            ) : (
                                <>
                                    <Ionicons name="checkmark-circle" size={20} color="#fff" />
                                    <Text style={styles.createButtonText}>Criar Consulta</Text>
                                </>
                            )}
                        </TouchableOpacity>
                    </ScrollView>
                </View>
            </Modal>
            {/* ==================== PATIENT CONSULTATIONS MODAL ==================== */}
            <Modal visible={patientConsultationsModalOpen} animationType="slide" presentationStyle="pageSheet">
                <View style={styles.modalContainer}>
                    <View style={styles.modalHeader}>
                        <Text style={styles.modalHeaderTitle} numberOfLines={1}>
                            Consultas - {selectedPatient?.patient.nome}
                        </Text>
                        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                            {selectedPatient && (
                                <TouchableOpacity
                                    style={{ flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: 'rgba(6, 182, 212, 0.15)', borderRadius: 10, paddingHorizontal: 12, paddingVertical: 6 }}
                                    onPress={() => {
                                        setPatientConsultationsModalOpen(false);
                                        startNewConsultationForPatient(selectedPatient.consultations[0]);
                                    }}
                                    activeOpacity={0.7}
                                >
                                    <Ionicons name="add-circle-outline" size={16} color="#06b6d4" />
                                    <Text style={{ color: '#06b6d4', fontSize: 13, fontWeight: '600' }}>Nova Consulta</Text>
                                </TouchableOpacity>
                            )}
                            <TouchableOpacity onPress={() => { setPatientConsultationsModalOpen(false); setSelectedPatient(null); }}>
                                <Ionicons name="close" size={24} color="#94a3b8" />
                            </TouchableOpacity>
                        </View>
                    </View>

                    <FlatList
                        data={selectedPatient?.consultations || []}
                        keyExtractor={(item) => item.id}
                        renderItem={renderConsultationItem}
                        contentContainerStyle={styles.listContent}
                        ListEmptyComponent={
                            <View style={styles.emptyContainer}>
                                <Text style={styles.emptyText}>Nenhuma consulta para este paciente</Text>
                            </View>
                        }
                    />
                </View>
            </Modal>

            {/* ==================== DETAIL MODAL ==================== */}
            <Modal visible={detailModalOpen} animationType="slide" presentationStyle="pageSheet">
                <View style={styles.modalContainer}>
                    <View style={styles.modalHeader}>
                        <Text style={styles.modalHeaderTitle} numberOfLines={1}>
                            Detalhes{selectedConsultation ? ` - ${selectedConsultation.patient.nome}` : ''}
                        </Text>
                        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                            {selectedConsultation && (
                                <TouchableOpacity
                                    style={{ flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: 'rgba(6, 182, 212, 0.15)', borderRadius: 10, paddingHorizontal: 12, paddingVertical: 6 }}
                                    onPress={() => startNewConsultationForPatient(selectedConsultation)}
                                    activeOpacity={0.7}
                                >
                                    <Ionicons name="add-circle-outline" size={16} color="#06b6d4" />
                                    <Text style={{ color: '#06b6d4', fontSize: 13, fontWeight: '600' }}>Nova Consulta</Text>
                                </TouchableOpacity>
                            )}
                            <TouchableOpacity onPress={() => { setDetailModalOpen(false); setSelectedConsultation(null); }}>
                                <Ionicons name="close" size={24} color="#94a3b8" />
                            </TouchableOpacity>
                        </View>
                    </View>

                    {selectedConsultation && (
                        <ScrollView style={styles.modalBody} contentContainerStyle={{ paddingBottom: 40 }}>
                            {/* Patient Info */}
                            <View style={styles.detailInfoSection}>
                                <View style={styles.detailInfoRow}>
                                    <Text style={styles.detailInfoLabel}>Paciente</Text>
                                    <Text style={styles.detailInfoValue}>{selectedConsultation.patient.nome}</Text>
                                </View>
                                <View style={styles.detailInfoRow}>
                                    <Text style={styles.detailInfoLabel}>CPF</Text>
                                    <Text style={styles.detailInfoValue}>{selectedConsultation.patient.cpf}</Text>
                                </View>
                                <View style={styles.detailInfoRow}>
                                    <Text style={styles.detailInfoLabel}>Data da Consulta</Text>
                                    <Text style={styles.detailInfoValue}>{formatDate(selectedConsultation.createdAt)}</Text>
                                </View>
                            </View>

                            {/* Divider */}
                            <View style={styles.divider} />

                            {/* Images */}
                            <Text style={styles.imagesTitle}>
                                Imagens ({selectedConsultation.images?.length || 0})
                            </Text>

                            {selectedConsultation.images && selectedConsultation.images.length > 0 ? (
                                selectedConsultation.images.map((image) => (
                                    <View key={image.id} style={styles.imageCard}>
                                        {/* Image */}
                                        <Image
                                            source={{ uri: getImageUrl(image.filePath) }}
                                            style={styles.detailImage}
                                            resizeMode="contain"
                                        />

                                        <View style={styles.imageCardContent}>
                                            <Text style={styles.imageFileName}>{image.fileName}</Text>
                                            {image.localizacao && (
                                                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4, marginBottom: 6 }}>
                                                    <Ionicons name="location-outline" size={14} color="#94a3b8" />
                                                    <Text style={{ color: '#94a3b8', fontSize: 13 }}>{LocalizacaoLabels[image.localizacao as Localizacao] || image.localizacao}</Text>
                                                </View>
                                            )}

                                            {/* AI Diagnosis */}
                                            {(image.aiDiagnosis || image.multClass) && (
                                                <View style={styles.aiDiagnosisBox}>
                                                    <View style={styles.aiDiagnosisHeader}>
                                                        <Ionicons name="analytics" size={16} color="#38bdf8" />
                                                        <Text style={styles.aiDiagnosisTitle}>Diagnóstico IA</Text>
                                                    </View>
                                                    {image.aiDiagnosis && (
                                                        <Text style={styles.aiDiagnosisText}>
                                                            Classificação: <Text style={styles.aiDiagnosisValue}>{image.aiDiagnosis}</Text>
                                                        </Text>
                                                    )}
                                                    {image.confidence != null && (
                                                        <Text style={styles.aiDiagnosisText}>
                                                            Confiança: <Text style={styles.aiDiagnosisValue}>{(image.confidence * 100).toFixed(1)}%</Text>
                                                        </Text>
                                                    )}
                                                    {image.multClass && (
                                                        <Text style={styles.aiDiagnosisText}>
                                                            Sub-classe: <Text style={styles.aiDiagnosisValue}>{image.multClass}</Text>
                                                        </Text>
                                                    )}
                                                    {image.multClassConfidence != null && (
                                                        <Text style={styles.aiDiagnosisText}>
                                                            Confiança Sub-classe: <Text style={styles.aiDiagnosisValue}>{(image.multClassConfidence * 100).toFixed(1)}%</Text>
                                                        </Text>
                                                    )}
                                                </View>
                                            )}

                                            {/* Confirmed or Confirm action */}
                                            {image.confirmed ? (
                                                <View>
                                                    <View style={styles.confirmedBanner}>
                                                        <Ionicons name="checkmark-circle" size={18} color="#22c55e" />
                                                        <Text style={styles.confirmedBannerText}>
                                                            Confirmado: {image.finalDiagnosis
                                                                ? (DoctorVerdictLabels[image.finalDiagnosis as DoctorVerdict] || image.finalDiagnosis)
                                                                : 'N/A'}
                                                        </Text>
                                                    </View>
                                                    <TouchableOpacity
                                                        style={styles.editVerdictButton}
                                                        onPress={() => setVerdictPickerImageId(image.id)}
                                                        activeOpacity={0.7}
                                                    >
                                                        <Ionicons name="create-outline" size={16} color="#f59e0b" />
                                                        <Text style={styles.editVerdictText}>Alterar Diagnóstico</Text>
                                                    </TouchableOpacity>
                                                </View>
                                            ) : (
                                                <View>
                                                    <TouchableOpacity
                                                        style={styles.verdictPickerButton}
                                                        onPress={() => setVerdictPickerImageId(image.id)}
                                                    >
                                                        <Text style={styles.verdictPickerText}>Selecione o diagnóstico</Text>
                                                        <Ionicons name="chevron-down" size={18} color="#64748b" />
                                                    </TouchableOpacity>
                                                </View>
                                            )}
                                        </View>
                                    </View>
                                ))
                            ) : (
                                <View style={styles.emptyContainer}>
                                    <Ionicons name="image-outline" size={36} color="#334155" />
                                    <Text style={styles.emptyText}>Nenhuma imagem nesta consulta</Text>
                                </View>
                            )}
                        </ScrollView>
                    )}
                </View>
            </Modal>

            {/* ==================== PICKER MODALS ==================== */}

            {/* Sex Picker */}
            <Modal visible={showSexPicker} transparent animationType="fade">
                <TouchableOpacity style={styles.pickerOverlay} onPress={() => setShowSexPicker(false)} activeOpacity={1}>
                    <View style={styles.pickerModal}>
                        <Text style={styles.pickerModalTitle}>Sexo</Text>
                        {[
                            { value: 'M', label: 'Masculino' },
                            { value: 'F', label: 'Feminino' },
                        ].map(item => (
                            <TouchableOpacity
                                key={item.value}
                                style={styles.pickerOption}
                                onPress={() => {
                                    setPatientData({ ...patientData, sexo: item.value as any });
                                    setShowSexPicker(false);
                                }}
                            >
                                <Text style={[styles.pickerOptionText, patientData.sexo === item.value && styles.pickerOptionSelected]}>
                                    {item.label}
                                </Text>
                                {patientData.sexo === item.value && <Ionicons name="checkmark" size={20} color="#06b6d4" />}
                            </TouchableOpacity>
                        ))}
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Date Picker Calendar Modal */}
            <Modal visible={showDatePicker} transparent animationType="fade">
                <TouchableOpacity style={styles.pickerOverlay} onPress={() => setShowDatePicker(false)} activeOpacity={1}>
                    <View style={[styles.pickerModal, { maxWidth: 380 }]}>
                        <Text style={styles.pickerModalTitle}>Data de Nascimento</Text>

                        {/* Month/Year navigation */}
                        <View style={styles.calendarNav}>
                            <TouchableOpacity onPress={() => {
                                if (datePickerMonth === 0) {
                                    setDatePickerMonth(11);
                                    setDatePickerYear(datePickerYear - 1);
                                } else {
                                    setDatePickerMonth(datePickerMonth - 1);
                                }
                            }}>
                                <Ionicons name="chevron-back" size={24} color="#06b6d4" />
                            </TouchableOpacity>
                            <Text style={styles.calendarNavTitle}>
                                {['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
                                    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'][datePickerMonth]} {datePickerYear}
                            </Text>
                            <TouchableOpacity onPress={() => {
                                if (datePickerMonth === 11) {
                                    setDatePickerMonth(0);
                                    setDatePickerYear(datePickerYear + 1);
                                } else {
                                    setDatePickerMonth(datePickerMonth + 1);
                                }
                            }}>
                                <Ionicons name="chevron-forward" size={24} color="#06b6d4" />
                            </TouchableOpacity>
                        </View>

                        {/* Year quick nav */}
                        <View style={styles.calendarYearNav}>
                            <TouchableOpacity onPress={() => setDatePickerYear(datePickerYear - 10)}>
                                <Text style={styles.calendarYearBtn}>-10</Text>
                            </TouchableOpacity>
                            <TouchableOpacity onPress={() => setDatePickerYear(datePickerYear - 1)}>
                                <Text style={styles.calendarYearBtn}>-1</Text>
                            </TouchableOpacity>
                            <Text style={styles.calendarYearText}>{datePickerYear}</Text>
                            <TouchableOpacity onPress={() => setDatePickerYear(datePickerYear + 1)}>
                                <Text style={styles.calendarYearBtn}>+1</Text>
                            </TouchableOpacity>
                            <TouchableOpacity onPress={() => setDatePickerYear(datePickerYear + 10)}>
                                <Text style={styles.calendarYearBtn}>+10</Text>
                            </TouchableOpacity>
                        </View>

                        {/* Day headers */}
                        <View style={styles.calendarRow}>
                            {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map(d => (
                                <Text key={d} style={styles.calendarDayHeader}>{d}</Text>
                            ))}
                        </View>

                        {/* Day grid */}
                        {(() => {
                            const firstDay = new Date(datePickerYear, datePickerMonth, 1).getDay();
                            const daysInMonth = new Date(datePickerYear, datePickerMonth + 1, 0).getDate();
                            const weeks: (number | null)[][] = [];
                            let week: (number | null)[] = Array(firstDay).fill(null);

                            for (let day = 1; day <= daysInMonth; day++) {
                                week.push(day);
                                if (week.length === 7) {
                                    weeks.push(week);
                                    week = [];
                                }
                            }
                            if (week.length > 0) {
                                while (week.length < 7) week.push(null);
                                weeks.push(week);
                            }

                            const selectedStr = patientData.dataNascimento;

                            return weeks.map((w, wi) => (
                                <View key={wi} style={styles.calendarRow}>
                                    {w.map((day, di) => {
                                        if (day === null) {
                                            return <View key={di} style={styles.calendarDayCell} />;
                                        }
                                        const dateStr = `${datePickerYear}-${String(datePickerMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                                        const isSelected = dateStr === selectedStr;
                                        const isToday = (() => {
                                            const now = new Date();
                                            return day === now.getDate() && datePickerMonth === now.getMonth() && datePickerYear === now.getFullYear();
                                        })();

                                        return (
                                            <TouchableOpacity
                                                key={di}
                                                style={[
                                                    styles.calendarDayCell,
                                                    isSelected && styles.calendarDaySelected,
                                                    isToday && !isSelected && styles.calendarDayToday,
                                                ]}
                                                onPress={() => {
                                                    setPatientData({ ...patientData, dataNascimento: dateStr });
                                                    setShowDatePicker(false);
                                                }}
                                            >
                                                <Text style={[
                                                    styles.calendarDayText,
                                                    isSelected && styles.calendarDayTextSelected,
                                                    isToday && !isSelected && styles.calendarDayTextToday,
                                                ]}>
                                                    {day}
                                                </Text>
                                            </TouchableOpacity>
                                        );
                                    })}
                                </View>
                            ));
                        })()}
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Location Picker (per-image) */}
            <Modal visible={showLocPicker} transparent animationType="fade">
                <TouchableOpacity style={styles.pickerOverlay} onPress={() => setShowLocPicker(false)} activeOpacity={1}>
                    <View style={styles.pickerModal}>
                        <Text style={styles.pickerModalTitle}>Localização da Lesão</Text>
                        <FlatList
                            data={localizacaoList}
                            keyExtractor={(item) => item.value}
                            renderItem={({ item }) => (
                                <TouchableOpacity
                                    style={styles.pickerOption}
                                    onPress={() => {
                                        setImageLocalizacoes(prev => {
                                            const updated = [...prev];
                                            updated[locPickerIndex] = item.value;
                                            return updated;
                                        });
                                        setShowLocPicker(false);
                                    }}
                                >
                                    <Text style={[styles.pickerOptionText, imageLocalizacoes[locPickerIndex] === item.value && styles.pickerOptionSelected]}>
                                        {item.label}
                                    </Text>
                                    {imageLocalizacoes[locPickerIndex] === item.value && <Ionicons name="checkmark" size={20} color="#06b6d4" />}
                                </TouchableOpacity>
                            )}
                            style={{ maxHeight: 400 }}
                        />
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Verdict Picker for individual image */}
            <Modal visible={verdictPickerImageId !== null} transparent animationType="fade">
                <TouchableOpacity style={styles.pickerOverlay} onPress={() => setVerdictPickerImageId(null)} activeOpacity={1}>
                    <View style={styles.pickerModal}>
                        <Text style={styles.pickerModalTitle}>Diagnóstico Médico</Text>
                        <FlatList
                            data={DoctorVerdictOptions}
                            keyExtractor={(item) => item.value}
                            renderItem={({ item }) => (
                                <TouchableOpacity
                                    style={styles.pickerOption}
                                    onPress={() => {
                                        if (verdictPickerImageId) {
                                            handleConfirmImageDiagnosis(verdictPickerImageId, item.value);
                                        }
                                    }}
                                >
                                    <Text style={styles.pickerOptionText}>{item.label}</Text>
                                    <Ionicons name="chevron-forward" size={16} color="#475569" />
                                </TouchableOpacity>
                            )}
                        />
                    </View>
                </TouchableOpacity>
            </Modal>
        </View>
    );
}

// ==================== STYLES ====================
const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0f172a',
    },
    // Header
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingTop: 16,
        paddingBottom: 12,
    },
    headerTitle: {
        fontSize: 22,
        fontWeight: '800',
        color: '#f1f5f9',
    },
    newButton: {
        backgroundColor: '#06b6d4',
        borderRadius: 12,
        height: 40,
        paddingHorizontal: 14,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        shadowColor: '#06b6d4',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 6,
    },
    newButtonText: {
        color: '#fff',
        fontSize: 14,
        fontWeight: '700',
    },
    // Error
    errorContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(239, 68, 68, 0.15)',
        padding: 12,
        borderRadius: 12,
        marginHorizontal: 20,
        marginBottom: 8,
        gap: 8,
    },
    errorText: {
        color: '#ef4444',
        fontSize: 13,
        flex: 1,
    },
    // Filters
    filterSection: {
        paddingHorizontal: 20,
        marginBottom: 8,
    },
    filterRow: {
        flexDirection: 'row',
        gap: 10,
        marginBottom: 8,
    },
    filterInput: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#1e293b',
        borderRadius: 12,
        borderWidth: 1,
        borderColor: '#334155',
        paddingHorizontal: 12,
        height: 42,
        gap: 8,
    },
    filterTextInput: {
        flex: 1,
        fontSize: 14,
        color: '#f1f5f9',
    },
    filterButtons: {
        flexDirection: 'row',
        gap: 10,
    },
    filterBtn: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(6, 182, 212, 0.15)',
        borderRadius: 10,
        paddingHorizontal: 14,
        height: 36,
        gap: 6,
    },
    filterBtnText: {
        color: '#06b6d4',
        fontSize: 13,
        fontWeight: '600',
    },
    clearBtn: {
        alignItems: 'center',
        justifyContent: 'center',
        paddingHorizontal: 14,
        height: 36,
    },
    clearBtnText: {
        color: '#64748b',
        fontSize: 13,
        fontWeight: '500',
    },
    // List
    listContent: {
        paddingHorizontal: 20,
        paddingBottom: 20,
    },
    consultationCard: {
        backgroundColor: '#1e293b',
        borderRadius: 16,
        padding: 16,
        marginBottom: 10,
        borderWidth: 1,
        borderColor: '#334155',
    },
    consultationCardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
        marginBottom: 12,
    },
    patientName: {
        fontSize: 16,
        fontWeight: '700',
        color: '#f1f5f9',
    },
    patientCpf: {
        fontSize: 13,
        color: '#64748b',
        marginTop: 2,
    },
    statusBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        borderRadius: 8,
        paddingHorizontal: 10,
        paddingVertical: 4,
        gap: 5,
    },
    statusDot: {
        width: 7,
        height: 7,
        borderRadius: 4,
    },
    statusText: {
        fontSize: 11,
        fontWeight: '600',
    },
    consultationCardFooter: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 14,
    },
    footerInfo: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 4,
    },
    footerText: {
        fontSize: 12,
        color: '#64748b',
    },
    // Empty & Loading
    centerContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingTop: 60,
    },
    loadingText: {
        color: '#64748b',
        fontSize: 14,
        marginTop: 12,
    },
    emptyContainer: {
        alignItems: 'center',
        paddingTop: 40,
        gap: 8,
    },
    emptyText: {
        color: '#475569',
        fontSize: 15,
        fontWeight: '600',
    },
    emptySubText: {
        color: '#334155',
        fontSize: 13,
    },
    loadingOverlay: {
        ...StyleSheet.absoluteFillObject,
        backgroundColor: 'rgba(15, 23, 42, 0.7)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    // ==================== Modal Shared ====================
    modalContainer: {
        flex: 1,
        backgroundColor: '#0f172a',
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingTop: 16,
        paddingBottom: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#1e293b',
    },
    modalHeaderTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#f1f5f9',
        flex: 1,
    },
    modalBody: {
        flex: 1,
        paddingHorizontal: 20,
    },
    // ==================== New Consultation Form ====================
    formSection: {
        marginTop: 20,
    },
    sectionHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        marginBottom: 14,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '700',
        color: '#f1f5f9',
    },
    inputGroup: {
        marginBottom: 14,
    },
    label: {
        fontSize: 13,
        fontWeight: '600',
        color: '#cbd5e1',
        marginBottom: 6,
        marginLeft: 4,
    },
    inputContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#1e293b',
        borderRadius: 14,
        borderWidth: 1,
        borderColor: '#334155',
        paddingHorizontal: 14,
    },
    input: {
        flex: 1,
        height: 48,
        fontSize: 15,
        color: '#f1f5f9',
    },
    row: {
        flexDirection: 'row',
    },
    pickerButton: {
        backgroundColor: '#1e293b',
        borderRadius: 14,
        borderWidth: 1,
        borderColor: '#334155',
        paddingHorizontal: 14,
        height: 48,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    pickerButtonText: {
        fontSize: 15,
        color: '#f1f5f9',
    },
    // Image picker
    imagePreviewContainer: {
        position: 'relative',
        marginBottom: 12,
    },
    imagePreview: {
        width: '100%',
        height: 200,
        borderRadius: 14,
        backgroundColor: '#1e293b',
    },
    removeImageBtn: {
        position: 'absolute',
        top: 8,
        right: 8,
    },
    imagePlaceholder: {
        backgroundColor: '#1e293b',
        borderRadius: 14,
        borderWidth: 2,
        borderStyle: 'dashed',
        borderColor: '#334155',
        padding: 32,
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 12,
    },
    imagePlaceholderText: {
        color: '#475569',
        fontSize: 14,
        marginTop: 10,
        textAlign: 'center',
    },
    imageButtons: {
        flexDirection: 'row',
        gap: 12,
        marginTop: 4,
    },
    imageButton: {
        flex: 1,
        backgroundColor: '#1e293b',
        borderRadius: 14,
        borderWidth: 1,
        borderColor: '#334155',
        height: 48,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 8,
    },
    imageButtonText: {
        color: '#06b6d4',
        fontSize: 14,
        fontWeight: '600',
    },
    // Create button
    createButton: {
        backgroundColor: '#06b6d4',
        borderRadius: 14,
        height: 52,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 8,
        marginTop: 24,
        shadowColor: '#06b6d4',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 6,
    },
    createButtonText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: '700',
    },
    buttonDisabled: {
        opacity: 0.5,
    },
    // ==================== Detail Modal ====================
    detailInfoSection: {
        backgroundColor: '#1e293b',
        borderRadius: 16,
        padding: 16,
        marginTop: 16,
        borderWidth: 1,
        borderColor: '#334155',
    },
    detailInfoRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 8,
    },
    detailInfoLabel: {
        fontSize: 13,
        color: '#64748b',
        fontWeight: '500',
    },
    detailInfoValue: {
        fontSize: 14,
        color: '#f1f5f9',
        fontWeight: '600',
    },
    divider: {
        height: 1,
        backgroundColor: '#334155',
        marginVertical: 20,
    },
    imagesTitle: {
        fontSize: 17,
        fontWeight: '700',
        color: '#f1f5f9',
        marginBottom: 14,
    },
    imageCard: {
        backgroundColor: '#1e293b',
        borderRadius: 16,
        marginBottom: 14,
        overflow: 'hidden',
        borderWidth: 1,
        borderColor: '#334155',
    },
    detailImage: {
        width: '100%',
        height: 200,
        backgroundColor: '#0f172a',
    },
    imageCardContent: {
        padding: 14,
    },
    imageFileName: {
        fontSize: 13,
        color: '#94a3b8',
        fontWeight: '600',
        marginBottom: 8,
    },
    // AI Diagnosis
    aiDiagnosisBox: {
        backgroundColor: 'rgba(56, 189, 248, 0.1)',
        borderRadius: 12,
        padding: 12,
        marginBottom: 10,
        borderWidth: 1,
        borderColor: 'rgba(56, 189, 248, 0.2)',
    },
    aiDiagnosisHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        marginBottom: 6,
    },
    aiDiagnosisTitle: {
        fontSize: 13,
        fontWeight: '700',
        color: '#38bdf8',
    },
    aiDiagnosisText: {
        fontSize: 12,
        color: '#94a3b8',
        marginTop: 2,
    },
    aiDiagnosisValue: {
        color: '#e2e8f0',
        fontWeight: '600',
    },
    // Confirmed
    confirmedBanner: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(34, 197, 94, 0.15)',
        padding: 12,
        borderRadius: 12,
        gap: 8,
    },
    confirmedBannerText: {
        color: '#22c55e',
        fontSize: 13,
        fontWeight: '600',
        flex: 1,
    },
    // Edit verdict
    editVerdictButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 6,
        marginTop: 8,
        paddingVertical: 8,
        borderRadius: 10,
        borderWidth: 1,
        borderColor: 'rgba(245, 158, 11, 0.3)',
        backgroundColor: 'rgba(245, 158, 11, 0.1)',
    },
    editVerdictText: {
        color: '#f59e0b',
        fontSize: 13,
        fontWeight: '600',
    },
    // Verdict picker button
    verdictPickerButton: {
        backgroundColor: '#0f172a',
        borderRadius: 12,
        borderWidth: 1,
        borderColor: '#475569',
        paddingHorizontal: 14,
        height: 44,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    verdictPickerText: {
        color: '#94a3b8',
        fontSize: 14,
    },
    // ==================== Picker Modals ====================
    pickerOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.6)',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
    },
    pickerModal: {
        backgroundColor: '#1e293b',
        borderRadius: 20,
        padding: 20,
        width: '100%',
        maxWidth: 360,
        borderWidth: 1,
        borderColor: '#334155',
    },
    pickerModalTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#f1f5f9',
        marginBottom: 16,
        textAlign: 'center',
    },
    pickerOption: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 14,
        paddingHorizontal: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#334155',
    },
    pickerOptionText: {
        fontSize: 15,
        color: '#cbd5e1',
    },
    pickerOptionSelected: {
        color: '#06b6d4',
        fontWeight: '700',
    },
    // Calendar date picker
    calendarNav: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 12,
        paddingHorizontal: 4,
    },
    calendarNavTitle: {
        fontSize: 16,
        fontWeight: '700',
        color: '#f1f5f9',
    },
    calendarYearNav: {
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 16,
        marginBottom: 14,
    },
    calendarYearBtn: {
        color: '#06b6d4',
        fontSize: 13,
        fontWeight: '700',
        paddingHorizontal: 8,
        paddingVertical: 4,
        backgroundColor: 'rgba(6, 182, 212, 0.15)',
        borderRadius: 6,
    },
    calendarYearText: {
        color: '#f1f5f9',
        fontSize: 15,
        fontWeight: '700',
    },
    calendarRow: {
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    calendarDayHeader: {
        width: 40,
        textAlign: 'center',
        fontSize: 12,
        fontWeight: '600',
        color: '#64748b',
        paddingVertical: 6,
    },
    calendarDayCell: {
        width: 40,
        height: 40,
        justifyContent: 'center',
        alignItems: 'center',
        borderRadius: 20,
    },
    calendarDaySelected: {
        backgroundColor: '#06b6d4',
    },
    calendarDayToday: {
        borderWidth: 1,
        borderColor: '#06b6d4',
    },
    calendarDayText: {
        fontSize: 14,
        color: '#cbd5e1',
    },
    calendarDayTextSelected: {
        color: '#fff',
        fontWeight: '700',
    },
    calendarDayTextToday: {
        color: '#06b6d4',
        fontWeight: '600',
    },
});
