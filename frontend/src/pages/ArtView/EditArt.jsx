import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ArtPost from '../../components/ArtPost/ArtPost';
import { artApi } from '../../api/artApi';

export default function EditArt() {
    const { id } = useParams();
    const [art, setArt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadArt = async () => {
            try {
                setLoading(true);
                setError(null);
                
                console.log(`Загрузка арта с ID: ${id}`);
                const data = await artApi.getArtById(id);
                
                if (!data) {
                    throw new Error('Арт не найден');
                }
                
                console.log('Арт загружен:', data);
                setArt(data);
            } catch (err) {
                console.error('Ошибка загрузки арта:', err);
                setError(err.message || 'Не удалось загрузить арт');
                setArt(null);
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            loadArt();
        } else {
            setError('ID арта не указан');
            setLoading(false);
        }
    }, [id]);

    if (loading) {
        return (
            <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '50vh' 
            }}>
                Загрузка...
            </div>
        );
    }
    
    if (error) {
        return (
            <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '50vh',
                color: 'red' 
            }}>
                Ошибка: {error}
            </div>
        );
    }
    
    if (!art) {
        return (
            <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '50vh' 
            }}>
                Арт не найден
            </div>
        );
    }

    return (
        <ArtPost 
            mode='edit'
            artId={art.id}
            image={art.imageUrl}
            description={art.description}
            tags={art.tags}
            owner={art.author}
        />
    );
}