import { Redirect } from 'expo-router';
import { useAuth } from '../contexts/AuthContext';

export default function Index() {
    const { isAuthenticated, user } = useAuth();

    if (isAuthenticated) {
        if (user?.accessLevel === 'ADMIN') {
            return <Redirect href="/(tabs)/admin" />;
        }
        return <Redirect href="/(tabs)/medico" />;
    }

    return <Redirect href="/login" />;
}
