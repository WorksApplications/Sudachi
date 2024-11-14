"""
Extract HTML from .warc file.
"""

import argparse as ap
from pathlib import Path
from tqdm import tqdm

from warcio.archiveiterator import ArchiveIterator


def parse_args() -> ap.Namespace:
    parser = ap.ArgumentParser()
    parser.add_argument("-i", "--input", type=Path,
                        help="input warc file")
    parser.add_argument("-o", "--output", type=Path, default="output.txt",
                        help="output text file")
    parser.add_argument("-n", "--num-records", type=int, default=None,
                        help="how many records to dump. dump all if not set.")

    args = parser.parse_args()
    return args


def main():
    args = parse_args()

    with args.input.open("rb") as fi, args.output.open("wb") as fo:
        count = 0
        for record in tqdm(ArchiveIterator(fi)):
            if (args.num_records is not None) and (count >= args.num_records):
                break

            try:
                if record.rec_type != "response":
                    continue
                contenttype = record.http_headers.get_header("Content-Type")
                if not contenttype.startswith("text/html"):
                    continue

                # dump raw html
                content = record.content_stream().read()
                fo.write(content)

                count += 1
            except:
                continue
    print(f"count: {count}")
    return


if __name__ == "__main__":
    main()
