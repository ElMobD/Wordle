import pathlib
import re
import urllib.request


SOURCE_URL = "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt"
WORD_REGEX = re.compile(r"^[a-z]+$")


def download_words(url: str) -> list[str]:
    with urllib.request.urlopen(url, timeout=30) as response:
        content = response.read().decode("utf-8")
    return content.splitlines()


def normalize(words: list[str]) -> list[str]:
    cleaned = set()
    for raw in words:
        word = raw.strip().lower()
        if WORD_REGEX.fullmatch(word):
            cleaned.add(word)
    return sorted(cleaned)


def write_wordlist(path: pathlib.Path, words: list[str]) -> None:
    path.write_text("\n".join(words) + "\n", encoding="utf-8")


def main() -> None:
    base_dir = pathlib.Path(__file__).resolve().parent
    daily_path = base_dir / "dailywords.txt"
    random_path = base_dir / "randomwords.txt"

    all_words = normalize(download_words(SOURCE_URL))

    # Daily mode: strict 5-letter dictionary.
    daily_words = [word for word in all_words if len(word) == 5]

    # Random/multiplayer mode: 4 to 8 letters.
    random_words = [word for word in all_words if 4 <= len(word) <= 8]

    write_wordlist(daily_path, daily_words)
    write_wordlist(random_path, random_words)

    print(f"Created {daily_path} with {len(daily_words)} words")
    print(f"Created {random_path} with {len(random_words)} words")


if __name__ == "__main__":
    main()
