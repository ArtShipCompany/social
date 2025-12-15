import ArtCard from '../ArtCard/ArtCard';
import styles from './Feed.module.css';

export const Feed = () => {
  const cards = Array.from({ length: 15 }, (_, i) => (
    <ArtCard key={i} />
  ));

  return (
    <div className={styles.feed}>
      {cards}
    </div>
  );
};

export default Feed;