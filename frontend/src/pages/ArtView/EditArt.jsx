import ArtPost from '../../components/ArtPost/ArtPost';
import img4 from '../../assets/mock-images/wenclair.jpg';

export default function EditArt() {
    return (
        <>
            <ArtPost edited={true} image={img4}/>
        </>
    )
}
