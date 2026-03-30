import React, { useEffect, useRef } from 'react';
import { Animated, Text, View, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

interface ToastProps {
    visible: boolean;
    type: ToastType;
    title: string;
    message?: string;
    duration?: number;
    onHide: () => void;
}

const TOAST_CONFIG: Record<ToastType, { bg: string; icon: keyof typeof Ionicons.glyphMap; iconBg: string }> = {
    success: { bg: '#16a34a', icon: 'checkmark-circle', iconBg: 'rgba(255,255,255,0.2)' },
    error: { bg: '#dc2626', icon: 'shield', iconBg: 'rgba(255,255,255,0.2)' },
    info: { bg: '#2563eb', icon: 'information-circle', iconBg: 'rgba(255,255,255,0.2)' },
    warning: { bg: '#d97706', icon: 'warning', iconBg: 'rgba(255,255,255,0.2)' },
};

export function Toast({ visible, type, title, message, duration = 3500, onHide }: ToastProps) {
    const translateY = useRef(new Animated.Value(-120)).current;
    const opacity = useRef(new Animated.Value(0)).current;
    const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        if (visible) {
            Animated.parallel([
                Animated.spring(translateY, {
                    toValue: 0,
                    useNativeDriver: true,
                    friction: 8,
                    tension: 60,
                }),
                Animated.timing(opacity, {
                    toValue: 1,
                    duration: 250,
                    useNativeDriver: true,
                }),
            ]).start();

            if (timerRef.current) clearTimeout(timerRef.current);
            timerRef.current = setTimeout(() => {
                hideToast();
            }, duration);
        }

        return () => {
            if (timerRef.current) clearTimeout(timerRef.current);
        };
    }, [visible]);

    const hideToast = () => {
        Animated.parallel([
            Animated.timing(translateY, {
                toValue: -120,
                duration: 300,
                useNativeDriver: true,
            }),
            Animated.timing(opacity, {
                toValue: 0,
                duration: 300,
                useNativeDriver: true,
            }),
        ]).start(() => {
            onHide();
        });
    };

    if (!visible) return null;

    const config = TOAST_CONFIG[type];

    return (
        <Animated.View
            style={[
                styles.container,
                { backgroundColor: config.bg, transform: [{ translateY }], opacity },
            ]}
        >
            <TouchableOpacity style={styles.content} onPress={hideToast} activeOpacity={0.9}>
                <View style={[styles.iconContainer, { backgroundColor: config.iconBg }]}>
                    <Ionicons name={config.icon} size={24} color="#fff" />
                </View>
                <View style={styles.textContainer}>
                    <Text style={styles.title}>{title}</Text>
                    {message ? <Text style={styles.message} numberOfLines={2}>{message}</Text> : null}
                </View>
                <Ionicons name="close" size={20} color="rgba(255,255,255,0.7)" />
            </TouchableOpacity>
        </Animated.View>
    );
}

// Hook for easy toast management
export function useToast() {
    const [toast, setToast] = React.useState<{
        visible: boolean;
        type: ToastType;
        title: string;
        message?: string;
    }>({ visible: false, type: 'info', title: '' });

    const showToast = (type: ToastType, title: string, message?: string) => {
        setToast({ visible: true, type, title, message });
    };

    const hideToast = () => {
        setToast(prev => ({ ...prev, visible: false }));
    };

    return { toast, showToast, hideToast };
}

const styles = StyleSheet.create({
    container: {
        position: 'absolute',
        top: 50,
        left: 16,
        right: 16,
        borderRadius: 16,
        zIndex: 9999,
        elevation: 20,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 6 },
        shadowOpacity: 0.35,
        shadowRadius: 12,
    },
    content: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 14,
        paddingHorizontal: 16,
        gap: 12,
    },
    iconContainer: {
        width: 40,
        height: 40,
        borderRadius: 12,
        justifyContent: 'center',
        alignItems: 'center',
    },
    textContainer: {
        flex: 1,
    },
    title: {
        color: '#fff',
        fontSize: 16,
        fontWeight: '800',
    },
    message: {
        color: 'rgba(255,255,255,0.85)',
        fontSize: 13,
        marginTop: 2,
    },
});
