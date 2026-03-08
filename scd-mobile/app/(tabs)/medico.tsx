import React, { useState } from 'react';
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
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import {
    consultationService,
    ConsultationResponse,
    DoctorVerdict,
    DoctorVerdictLabels,
    DoctorVerdictOptions,
} from '../../services/consultationService';

type Step = 'patient' | 'image' | 'result';

interface PatientData {
    nome: string;
    cpf: string;
    dataNascimento: string;
    sexo: 'M' | 'F' | 'OUTRO';
}

export default function MedicoDashboardScreen() {
    const [step, setStep] = useState<Step>('patient');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    // Patient data
    const [patientData, setPatientData] = useState<PatientData>({
        nome: '',
        cpf: '',
        dataNascimento: '',
        sexo: 'M',
    });

    // Image data — now supports multiple images
    const [images, setImages] = useState<{ uri: string; name: string; type: string }[]>([]);
    const [localizacao, setLocalizacao] = useState('');

    // Result data — now a ConsultationResponse
    const [result, setResult] = useState<ConsultationResponse | null>(null);

    // Verdict modal
    const [verdictModalOpen, setVerdictModalOpen] = useState(false);
    const [selectedVerdict, setSelectedVerdict] = useState<DoctorVerdict | null>(null);

    const localizacoes = [
        'Cabeça', 'Pescoço', 'Tronco', 'Braço Direito', 'Braço Esquerdo',
        'Mão Direita', 'Mão Esquerda', 'Perna Direita', 'Perna Esquerda',
        'Pé Direito', 'Pé Esquerdo', 'Costas', 'Abdômen',
    ];

    const [showLocPicker, setShowLocPicker] = useState(false);
    const [showSexPicker, setShowSexPicker] = useState(false);

    // ========== STEP 1: Patient Data ==========
    const handleProceedToImage = () => {
        if (!patientData.nome || !patientData.cpf || !patientData.sexo) {
            setError('Nome, CPF e Sexo são obrigatórios');
            return;
        }
        setError('');
        setStep('image');
    };

    // ========== STEP 2: Image Selection ==========
    const pickImage = async () => {
        const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
        if (status !== 'granted') {
            Alert.alert('Permissão necessária', 'Precisamos de acesso à galeria para selecionar imagens.');
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
        }
    };

    const takePhoto = async () => {
        const { status } = await ImagePicker.requestCameraPermissionsAsync();
        if (status !== 'granted') {
            Alert.alert('Permissão necessária', 'Precisamos de acesso à câmera para tirar fotos.');
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
        }
    };

    const removeImage = (index: number) => {
        setImages(prev => prev.filter((_, i) => i !== index));
    };

    const handleCreateConsultation = async () => {
        if (images.length === 0 || !localizacao) {
            setError('Selecione pelo menos uma imagem e informe a localização da lesão');
            return;
        }

        setError('');
        setLoading(true);

        try {
            const response = await consultationService.createConsultation({
                nome: patientData.nome,
                cpf: patientData.cpf,
                sexo: patientData.sexo,
                dataNascimento: patientData.dataNascimento || undefined,
                localizacao,
                images,
            });
            setResult(response);
            setStep('result');
        } catch (err: any) {
            setError(err.response?.data?.error || err.response?.data?.detail || 'Erro ao criar consulta');
        } finally {
            setLoading(false);
        }
    };

    // ========== STEP 3: Confirm Diagnosis ==========
    const handleConfirmDiagnosis = async () => {
        if (!result || !selectedVerdict) {
            setError('Selecione um diagnóstico antes de confirmar');
            return;
        }

        setError('');
        setLoading(true);

        try {
            const updated = await consultationService.confirmConsultationDiagnosis(result.id, selectedVerdict);
            setResult(updated);
            Alert.alert('Sucesso', 'Diagnóstico confirmado com sucesso!');
        } catch (err: any) {
            setError(err.response?.data?.error || 'Erro ao confirmar diagnóstico');
        } finally {
            setLoading(false);
        }
    };

    const handleNewConsultation = () => {
        setStep('patient');
        setPatientData({ nome: '', cpf: '', dataNascimento: '', sexo: 'M' });
        setImages([]);
        setLocalizacao('');
        setResult(null);
        setSelectedVerdict(null);
        setError('');
    };

    // ========== RENDER ==========
    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
            {/* Progress Steps */}
            <View style={styles.progressBar}>
                {(['patient', 'image', 'result'] as Step[]).map((s, index) => (
                    <React.Fragment key={s}>
                        <View style={[styles.progressDot, step === s && styles.progressDotActive, (['patient', 'image', 'result'].indexOf(step) > index) && styles.progressDotDone]}>
                            <Text style={[styles.progressDotText, (step === s || ['patient', 'image', 'result'].indexOf(step) > index) && styles.progressDotTextActive]}>
                                {index + 1}
                            </Text>
                        </View>
                        {index < 2 && (
                            <View style={[styles.progressLine, ['patient', 'image', 'result'].indexOf(step) > index && styles.progressLineDone]} />
                        )}
                    </React.Fragment>
                ))}
            </View>
            <View style={styles.progressLabels}>
                <Text style={[styles.progressLabel, step === 'patient' && styles.progressLabelActive]}>Paciente</Text>
                <Text style={[styles.progressLabel, step === 'image' && styles.progressLabelActive]}>Imagem</Text>
                <Text style={[styles.progressLabel, step === 'result' && styles.progressLabelActive]}>Resultado</Text>
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

            {/* STEP 1: Patient Data */}
            {step === 'patient' && (
                <View style={styles.card}>
                    <View style={styles.cardHeader}>
                        <Ionicons name="person-add-outline" size={24} color="#06b6d4" />
                        <Text style={styles.cardTitle}>Dados do Paciente</Text>
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
                                    {patientData.sexo === 'M' ? 'Masculino' : patientData.sexo === 'F' ? 'Feminino' : 'Outro'}
                                </Text>
                                <Ionicons name="chevron-down" size={18} color="#64748b" />
                            </TouchableOpacity>
                        </View>
                        <View style={[styles.inputGroup, { flex: 1, marginLeft: 8 }]}>
                            <Text style={styles.label}>Data Nasc.</Text>
                            <View style={styles.inputContainer}>
                                <TextInput
                                    style={styles.input}
                                    placeholder="AAAA-MM-DD"
                                    placeholderTextColor="#64748b"
                                    value={patientData.dataNascimento}
                                    onChangeText={(v) => setPatientData({ ...patientData, dataNascimento: v })}
                                />
                            </View>
                        </View>
                    </View>

                    <View style={styles.buttonRow}>
                        <TouchableOpacity
                            style={styles.primaryButton}
                            onPress={handleProceedToImage}
                            activeOpacity={0.8}
                        >
                            <Ionicons name="arrow-forward" size={20} color="#fff" />
                            <Text style={styles.primaryButtonText}>Prosseguir</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            )}

            {/* STEP 2: Image Selection & Classification */}
            {step === 'image' && (
                <View style={styles.card}>
                    <View style={styles.cardHeader}>
                        <Ionicons name="camera-outline" size={24} color="#06b6d4" />
                        <Text style={styles.cardTitle}>Imagens da Lesão</Text>
                    </View>

                    <View style={styles.imagePickerArea}>
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

                    <View style={styles.inputGroup}>
                        <Text style={styles.label}>Localização da Lesão *</Text>
                        <TouchableOpacity
                            style={styles.pickerButton}
                            onPress={() => setShowLocPicker(true)}
                        >
                            <Text style={[styles.pickerButtonText, !localizacao && { color: '#64748b' }]}>
                                {localizacao || 'Selecione a localização'}
                            </Text>
                            <Ionicons name="chevron-down" size={18} color="#64748b" />
                        </TouchableOpacity>
                    </View>

                    <View style={styles.buttonRow}>
                        <TouchableOpacity
                            style={styles.outlineButton}
                            onPress={() => setStep('patient')}
                            activeOpacity={0.7}
                        >
                            <Text style={styles.outlineButtonText}>← Voltar</Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={[styles.primaryButton, { flex: 1 }, (loading || images.length === 0 || !localizacao) && styles.buttonDisabled]}
                            onPress={handleCreateConsultation}
                            disabled={loading || images.length === 0 || !localizacao}
                            activeOpacity={0.8}
                        >
                            {loading ? (
                                <ActivityIndicator color="#fff" size="small" />
                            ) : (
                                <>
                                    <Ionicons name="analytics" size={20} color="#fff" />
                                    <Text style={styles.primaryButtonText}>Criar Consulta</Text>
                                </>
                            )}
                        </TouchableOpacity>
                    </View>
                </View>
            )}

            {/* STEP 3: Result & Confirmation */}
            {step === 'result' && result && (
                <View style={styles.card}>
                    <View style={styles.cardHeader}>
                        <Ionicons name="clipboard-outline" size={24} color="#06b6d4" />
                        <Text style={styles.cardTitle}>Resultado da IA</Text>
                    </View>

                    {images.length > 0 && (
                        <Image source={{ uri: images[0].uri }} style={styles.resultImage} resizeMode="contain" />
                    )}

                    {/* AI Diagnosis Card */}
                    <View style={styles.diagnosisCard}>
                        <View style={styles.diagnosisRow}>
                            <Text style={styles.diagnosisLabel}>Classificação:</Text>
                            <View style={[styles.diagnosisBadge,
                            result.aiDiagnosis?.toLowerCase() === 'maligno' ? styles.badgeDanger : styles.badgeSuccess
                            ]}>
                                <Text style={styles.diagnosisBadgeText}>
                                    {result.aiDiagnosis || 'N/A'}
                                </Text>
                            </View>
                        </View>

                        {result.confidence != null && (
                            <View style={styles.diagnosisRow}>
                                <Text style={styles.diagnosisLabel}>Confiança:</Text>
                                <Text style={styles.diagnosisValue}>
                                    {(result.confidence * 100).toFixed(1)}%
                                </Text>
                            </View>
                        )}

                        {result.multClass && (
                            <View style={styles.diagnosisRow}>
                                <Text style={styles.diagnosisLabel}>Sub-classe:</Text>
                                <Text style={styles.diagnosisValue}>{result.multClass}</Text>
                            </View>
                        )}

                        {result.multClassConfidence != null && (
                            <View style={styles.diagnosisRow}>
                                <Text style={styles.diagnosisLabel}>Confiança Sub-classe:</Text>
                                <Text style={styles.diagnosisValue}>
                                    {(result.multClassConfidence * 100).toFixed(1)}%
                                </Text>
                            </View>
                        )}
                    </View>

                    {/* Doctor Verdict */}
                    {result.confirmed ? (
                        <View style={styles.confirmedBanner}>
                            <Ionicons name="checkmark-circle" size={22} color="#22c55e" />
                            <Text style={styles.confirmedBannerText}>
                                Diagnóstico Confirmado: {result.finalDiagnosis ? DoctorVerdictLabels[result.finalDiagnosis as DoctorVerdict] || result.finalDiagnosis : 'N/A'}
                            </Text>
                        </View>
                    ) : (
                        <View>
                            <Text style={styles.label}>Confirmar Diagnóstico</Text>
                            <TouchableOpacity
                                style={styles.pickerButton}
                                onPress={() => setVerdictModalOpen(true)}
                            >
                                <Text style={[styles.pickerButtonText, !selectedVerdict && { color: '#64748b' }]}>
                                    {selectedVerdict ? DoctorVerdictLabels[selectedVerdict] : 'Selecione o diagnóstico'}
                                </Text>
                                <Ionicons name="chevron-down" size={18} color="#64748b" />
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={[styles.confirmButton, (!selectedVerdict || loading) && styles.buttonDisabled]}
                                onPress={handleConfirmDiagnosis}
                                disabled={!selectedVerdict || loading}
                                activeOpacity={0.8}
                            >
                                {loading ? (
                                    <ActivityIndicator color="#fff" size="small" />
                                ) : (
                                    <>
                                        <Ionicons name="checkmark-circle" size={20} color="#fff" />
                                        <Text style={styles.confirmButtonText}>Confirmar Diagnóstico</Text>
                                    </>
                                )}
                            </TouchableOpacity>
                        </View>
                    )}

                    <TouchableOpacity
                        style={[styles.primaryButton, { marginTop: 16 }]}
                        onPress={handleNewConsultation}
                        activeOpacity={0.8}
                    >
                        <Ionicons name="add-circle" size={20} color="#fff" />
                        <Text style={styles.primaryButtonText}>Nova Consulta</Text>
                    </TouchableOpacity>
                </View>
            )}

            {/* Sex Picker Modal */}
            <Modal visible={showSexPicker} transparent animationType="fade">
                <TouchableOpacity style={styles.modalOverlay} onPress={() => setShowSexPicker(false)} activeOpacity={1}>
                    <View style={styles.modalContent}>
                        <Text style={styles.modalTitle}>Sexo</Text>
                        {[
                            { value: 'M', label: 'Masculino' },
                            { value: 'F', label: 'Feminino' },
                            { value: 'OUTRO', label: 'Outro' },
                        ].map(item => (
                            <TouchableOpacity
                                key={item.value}
                                style={styles.modalOption}
                                onPress={() => {
                                    setPatientData({ ...patientData, sexo: item.value as any });
                                    setShowSexPicker(false);
                                }}
                            >
                                <Text style={[styles.modalOptionText, patientData.sexo === item.value && styles.modalOptionTextSelected]}>
                                    {item.label}
                                </Text>
                                {patientData.sexo === item.value && <Ionicons name="checkmark" size={20} color="#06b6d4" />}
                            </TouchableOpacity>
                        ))}
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Location Picker Modal */}
            <Modal visible={showLocPicker} transparent animationType="fade">
                <TouchableOpacity style={styles.modalOverlay} onPress={() => setShowLocPicker(false)} activeOpacity={1}>
                    <View style={styles.modalContent}>
                        <Text style={styles.modalTitle}>Localização da Lesão</Text>
                        <FlatList
                            data={localizacoes}
                            keyExtractor={(item) => item}
                            renderItem={({ item }) => (
                                <TouchableOpacity
                                    style={styles.modalOption}
                                    onPress={() => {
                                        setLocalizacao(item);
                                        setShowLocPicker(false);
                                    }}
                                >
                                    <Text style={[styles.modalOptionText, localizacao === item && styles.modalOptionTextSelected]}>
                                        {item}
                                    </Text>
                                    {localizacao === item && <Ionicons name="checkmark" size={20} color="#06b6d4" />}
                                </TouchableOpacity>
                            )}
                            style={{ maxHeight: 400 }}
                        />
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Verdict Picker Modal */}
            <Modal visible={verdictModalOpen} transparent animationType="fade">
                <TouchableOpacity style={styles.modalOverlay} onPress={() => setVerdictModalOpen(false)} activeOpacity={1}>
                    <View style={styles.modalContent}>
                        <Text style={styles.modalTitle}>Diagnóstico Médico</Text>
                        <FlatList
                            data={DoctorVerdictOptions}
                            keyExtractor={(item) => item.value}
                            renderItem={({ item }) => (
                                <TouchableOpacity
                                    style={styles.modalOption}
                                    onPress={() => {
                                        setSelectedVerdict(item.value);
                                        setVerdictModalOpen(false);
                                    }}
                                >
                                    <Text style={[styles.modalOptionText, selectedVerdict === item.value && styles.modalOptionTextSelected]}>
                                        {item.label}
                                    </Text>
                                    {selectedVerdict === item.value && <Ionicons name="checkmark" size={20} color="#06b6d4" />}
                                </TouchableOpacity>
                            )}
                        />
                    </View>
                </TouchableOpacity>
            </Modal>
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
    // Progress bar
    progressBar: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 4,
    },
    progressDot: {
        width: 32,
        height: 32,
        borderRadius: 16,
        backgroundColor: '#334155',
        justifyContent: 'center',
        alignItems: 'center',
    },
    progressDotActive: {
        backgroundColor: '#06b6d4',
    },
    progressDotDone: {
        backgroundColor: '#22c55e',
    },
    progressDotText: {
        fontSize: 13,
        fontWeight: '700',
        color: '#64748b',
    },
    progressDotTextActive: {
        color: '#fff',
    },
    progressLine: {
        height: 3,
        width: 60,
        backgroundColor: '#334155',
        marginHorizontal: 4,
    },
    progressLineDone: {
        backgroundColor: '#22c55e',
    },
    progressLabels: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingHorizontal: 16,
        marginBottom: 20,
    },
    progressLabel: {
        fontSize: 11,
        color: '#64748b',
        fontWeight: '600',
    },
    progressLabelActive: {
        color: '#06b6d4',
    },
    // Cards
    card: {
        backgroundColor: '#1e293b',
        borderRadius: 20,
        padding: 20,
        borderWidth: 1,
        borderColor: '#334155',
    },
    cardHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 10,
        marginBottom: 20,
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#f1f5f9',
    },
    // Inputs
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
        backgroundColor: '#0f172a',
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
        backgroundColor: '#0f172a',
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
    // Errors
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
    successBanner: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(34, 197, 94, 0.15)',
        padding: 12,
        borderRadius: 12,
        marginBottom: 14,
        gap: 8,
    },
    successBannerText: {
        color: '#22c55e',
        fontSize: 13,
        fontWeight: '600',
    },
    // Buttons
    buttonRow: {
        flexDirection: 'row',
        gap: 10,
        marginTop: 8,
    },
    primaryButton: {
        backgroundColor: '#06b6d4',
        borderRadius: 14,
        height: 48,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 8,
        flex: 1,
        shadowColor: '#06b6d4',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 6,
    },
    primaryButtonText: {
        color: '#fff',
        fontSize: 15,
        fontWeight: '700',
    },
    secondaryButton: {
        backgroundColor: '#334155',
        borderRadius: 14,
        height: 48,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 20,
    },
    secondaryButtonText: {
        color: '#06b6d4',
        fontSize: 15,
        fontWeight: '700',
    },
    outlineButton: {
        borderWidth: 1,
        borderColor: '#475569',
        borderRadius: 14,
        height: 48,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 16,
    },
    outlineButtonText: {
        color: '#94a3b8',
        fontSize: 14,
        fontWeight: '600',
    },
    buttonDisabled: {
        opacity: 0.5,
    },
    // Image Picker
    imagePickerArea: {
        marginBottom: 14,
    },
    imagePreviewContainer: {
        position: 'relative',
        marginBottom: 12,
    },
    imagePreview: {
        width: '100%',
        height: 220,
        borderRadius: 14,
        backgroundColor: '#0f172a',
    },
    removeImageBtn: {
        position: 'absolute',
        top: 8,
        right: 8,
    },
    imagePlaceholder: {
        backgroundColor: '#0f172a',
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
    },
    imageButton: {
        flex: 1,
        backgroundColor: '#0f172a',
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
    // Result
    resultImage: {
        width: '100%',
        height: 200,
        borderRadius: 14,
        backgroundColor: '#0f172a',
        marginBottom: 16,
    },
    diagnosisCard: {
        backgroundColor: '#0f172a',
        borderRadius: 14,
        padding: 16,
        marginBottom: 16,
        borderWidth: 1,
        borderColor: '#334155',
    },
    diagnosisRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 8,
        borderBottomWidth: 1,
        borderBottomColor: '#1e293b',
    },
    diagnosisLabel: {
        fontSize: 14,
        color: '#94a3b8',
        fontWeight: '500',
    },
    diagnosisValue: {
        fontSize: 14,
        color: '#f1f5f9',
        fontWeight: '600',
    },
    diagnosisBadge: {
        paddingHorizontal: 12,
        paddingVertical: 4,
        borderRadius: 8,
    },
    badgeDanger: {
        backgroundColor: 'rgba(239, 68, 68, 0.2)',
    },
    badgeSuccess: {
        backgroundColor: 'rgba(34, 197, 94, 0.2)',
    },
    diagnosisBadgeText: {
        fontSize: 14,
        fontWeight: '700',
        color: '#f1f5f9',
        textTransform: 'uppercase',
    },
    confirmedBanner: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(34, 197, 94, 0.15)',
        padding: 16,
        borderRadius: 14,
        gap: 10,
        marginBottom: 8,
    },
    confirmedBannerText: {
        color: '#22c55e',
        fontSize: 14,
        fontWeight: '600',
        flex: 1,
    },
    confirmButton: {
        backgroundColor: '#22c55e',
        borderRadius: 14,
        height: 48,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 8,
        marginTop: 12,
        shadowColor: '#22c55e',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 6,
    },
    confirmButtonText: {
        color: '#fff',
        fontSize: 15,
        fontWeight: '700',
    },
    // Modal
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.6)',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
    },
    modalContent: {
        backgroundColor: '#1e293b',
        borderRadius: 20,
        padding: 20,
        width: '100%',
        maxWidth: 360,
        borderWidth: 1,
        borderColor: '#334155',
    },
    modalTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#f1f5f9',
        marginBottom: 16,
        textAlign: 'center',
    },
    modalOption: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 14,
        paddingHorizontal: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#334155',
    },
    modalOptionText: {
        fontSize: 15,
        color: '#cbd5e1',
    },
    modalOptionTextSelected: {
        color: '#06b6d4',
        fontWeight: '700',
    },
});
