import styles from './EditArt.module.css';
import ArtPost from '../../components/ArtPost/ArtPost';

export default function EditArt() {
    return (
        <>
            <ArtPost edited={true}/>
        </>
    )
}