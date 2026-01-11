import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { mockArtsMap } from '../../mock-images/mockArts';
import ArtPost from '../../components/ArtPost/ArtPost';

// мок
async function fetchArtById(id) {
  return new Promise(resolve => setTimeout(() => resolve(mockArtsMap[id]), 300));
}

export default function EditArt() {
    const { id } = useParams();
    const [art, setArt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadArt = async () => {
            try {
                setLoading(true);
                const data = await fetchArtById(id);
                if (!data) throw new Error('Art not found');
                setArt(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        if (id) loadArt();
    }, [id]);

    if (loading) return <div>Загрузка...</div>;
    if (error) return <div>Ошибка: {error}</div>;
    if (!art) return <div>Арт не найден</div>;

    return (
        <ArtPost 
            edited={true}
            image={art.image}
            description={art.description}
            tags={art.tags}
        />
    );
}
