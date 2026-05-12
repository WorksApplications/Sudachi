#!/usr/bin/env python3

# Usage examples:
#   Convert split system lexicon CSVs while keeping the split layout:
#     python3 scripts/migrate_lexicon_v0_to_v1.py \
#         -o out_dir \
#         -p src/main/resources/pos.csv \
#         --drop-leading-zero-synonym-group \
#         small_lex.csv core_lex.csv notcore_lex.csv
#
#   Convert user lexicon CSV with split system lexicon references:
#     python3 scripts/migrate_v0_lexicon_to_v1.py \
#         -o outfile.csv \
#         -p src/main/resources/pos.csv \
#         --drop-leading-zero-synonym-group \
#         -s system_small.csv -s system_core.csv \
#         user.csv
#
# Note:
#   In v0, normalized form is a field associated with the entry itself, while
#   in v1 it points to a specific entry. This script converts normalized form
#   to the first entry whose headword matches, without considering any fields
#   other than headword.

import argparse as ap
import csv
import re
from pathlib import Path

try:
    from tqdm import tqdm
except ImportError:
    def tqdm(v):
        return v


POS = tuple[str, str, str, str, str, str]
POS_PARTS = ["POS1", "POS2", "POS3", "POS4", "POS5", "POS6"]
POS_ID_COLUMN = "POS_ID"
REFERENCE_ID_COLUMN = "REFERENCE_ID"
ROW_INDEX = "__ROW_INDEX"
WORDREF_DELIMITER = ","
LIST_DELIMITER = "/"
UNICODE_ESCAPE_RE = re.compile(r"\\u\{([0-9a-fA-F]+)\}")
NUMERIC_REF_RE = re.compile(r"^U?\d+$")


V0_COLUMNS = [
    "INDEX_FORM",
    "LEFT_ID",
    "RIGHT_ID",
    "COST",
    "HEADWORD",
    "POS1",
    "POS2",
    "POS3",
    "POS4",
    "POS5",
    "POS6",
    "READING_FORM",
    "NORMALIZED_FORM",
    "DICTIONARY_FORM",
    "MODE",
    "SPLIT_A",
    "SPLIT_B",
    "WORD_STRUCTURE",
    "SYNONYM_GROUPS",
    "SPLIT_C",
    "USER_DATA",
    "POS_ID",
    "REFERENCE_ID",
]

V1_COLUMNS = [
    "INDEX_FORM",
    "LEFT_ID",
    "RIGHT_ID",
    "COST",
    "HEADWORD",
    "POS1",
    "POS2",
    "POS3",
    "POS4",
    "POS5",
    "POS6",
    "READING_FORM",
    "NORMALIZED_FORM",
    "DICTIONARY_FORM",
    "MODE",
    "SPLIT_A",
    "SPLIT_B",
    "WORD_STRUCTURE",
    "SYNONYM_GROUPS",
    "SPLIT_C",
    "USER_DATA",
    "POS_ID",
    "REFERENCE_ID",
]

OUTPUT_V1_COLUMNS_WITH_POS_PARTS = [
    "INDEX_FORM",
    "LEFT_ID",
    "RIGHT_ID",
    "COST",
    "HEADWORD",
    "POS1",
    "POS2",
    "POS3",
    "POS4",
    "POS5",
    "POS6",
    "READING_FORM",
    "NORMALIZED_FORM",
    "DICTIONARY_FORM",
    "SPLIT_A",
    "SPLIT_B",
    "SPLIT_C",
    "WORD_STRUCTURE",
    "SYNONYM_GROUPS",
    "USER_DATA",
    "REFERENCE_ID",
]

OUTPUT_V1_COLUMNS_WITH_POS_ID = [
    "INDEX_FORM",
    "LEFT_ID",
    "RIGHT_ID",
    "COST",
    "HEADWORD",
    "POS_ID",
    "READING_FORM",
    "NORMALIZED_FORM",
    "DICTIONARY_FORM",
    "SPLIT_A",
    "SPLIT_B",
    "SPLIT_C",
    "WORD_STRUCTURE",
    "SYNONYM_GROUPS",
    "USER_DATA",
    "REFERENCE_ID",
]


def parse_args() -> ap.Namespace:
    parser = ap.ArgumentParser(
        description="Convert Sudachi v0 lexicon CSV into v1 format."
    )
    parser.add_argument(
        "lexicon",
        type=Path,
        nargs="+",
        help="input v0 lexicon csv file path(s)",
    )
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        nargs="+",
        default=None,
        help="output csv file path(s). For multiple inputs, either specify one output directory or one path per input.",
    )
    parser.add_argument(
        "-p",
        "--pos",
        type=Path,
        default=None,
        help="part-of-speech csv file path. If set, also emits POS_ID column.",
    )
    parser.add_argument(
        "--drop-leading-zero-synonym-group",
        action="store_true",
        help="drop leading zeros from each synonym_groups element written as slash-delimited integers",
    )
    parser.add_argument(
        "-s",
        "--system",
        type=Path,
        action="append",
        default=None,
        help="reference system lexicon csv file path(s) in v1 format. Specify multiple times to concatenate files in order.",
    )
    return parser.parse_args()


def normalize_column_name(name: str) -> str:
    return name.replace("_", "").upper()


def unescape(text: str) -> str:
    return UNICODE_ESCAPE_RE.sub(lambda m: chr(int(m.group(1), 16)), text)


def escape_wordref_part(value: str) -> str:
    value = value.replace(LIST_DELIMITER, r"\u{2f}")
    value = value.replace(WORDREF_DELIMITER, r"\u{2c}")
    return value


def is_header_row(row: list[str], expected: list[str]) -> bool:
    if len(row) < 2:
        return False
    normalized = {normalize_column_name(v) for v in row}
    expected_normalized = {normalize_column_name(v) for v in expected}
    return normalized.issubset(expected_normalized)


def row_to_dict(row: list[str], columns: list[str], mapping: list[str]=None) -> dict[str, str]:
    if mapping is None:
        mapping = columns

    entry = {column: "" for column in columns}
    for i, value in enumerate(row[: len(mapping)]):
        entry[mapping[i]] = value
    return entry


def load_csv_rows(path: Path, default_columns: list[str]) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8") as fi:
        reader = csv.reader(fi)
        try:
            first_row = next(reader)
        except StopIteration:
            return []

        rows: list[dict[str, str]] = []
        if is_header_row(first_row, default_columns):
            mapping = []
            for name in first_row:
                normalized = normalize_column_name(name)
                canonical = None
                for column in default_columns:
                    if normalize_column_name(column) == normalized:
                        canonical = column
                        break
                if canonical is None:
                    raise ValueError(f"Unknown column name in {path}: {name}")
                mapping.append(canonical)
        else:
            mapping = default_columns
            rows.append(row_to_dict(first_row, default_columns))

        for row in reader:
            rows.append(row_to_dict(row, default_columns, mapping))
        return rows


def load_csv_rows_from_paths(
    paths: list[Path],
    default_columns: list[str],
) -> tuple[list[dict[str, str]], list[list[dict[str, str]]]]:
    chunks: list[list[dict[str, str]]] = []
    all_rows: list[dict[str, str]] = []
    for path in paths:
        rows = load_csv_rows(path, default_columns)
        chunks.append(rows)
        all_rows.extend(rows)
    for i, row in enumerate(all_rows):
        row[ROW_INDEX] = str(i)
    return all_rows, chunks


def load_pos(path: Path | None) -> dict[POS, int]:
    if path is None:
        return {}

    with path.open(newline="", encoding="utf-8") as fi:
        reader = csv.reader(fi)
        try:
            first_row = next(reader)
        except StopIteration:
            return {}

        pos_rows: list[dict[str, str]] = []
        normalized_first = [normalize_column_name(v) for v in first_row]
        if POS_ID_COLUMN in normalized_first or "POS1" in normalized_first:
            rows = load_csv_rows(path, [POS_ID_COLUMN] + POS_PARTS)
            pos_rows.extend(rows)
        elif len(first_row) == 6:
            pos_rows.append(
                {POS_ID_COLUMN: "", **dict(zip(POS_PARTS, first_row))})
            for row in reader:
                pos_rows.append(
                    {POS_ID_COLUMN: "", **dict(zip(POS_PARTS, row))})
        elif len(first_row) == 7:
            pos_rows.append(
                {POS_ID_COLUMN: first_row[0], **dict(zip(POS_PARTS, first_row[1:7]))})
            for row in reader:
                pos_rows.append(
                    {POS_ID_COLUMN: row[0], **dict(zip(POS_PARTS, row[1:7]))})
        else:
            raise ValueError(f"Invalid POS csv format: {path}")

    posmap: dict[POS, int] = {}
    for i, row in enumerate(pos_rows):
        pos = get_pos(row)
        pos_id = row.get(POS_ID_COLUMN, "")
        posmap[pos] = int(pos_id) if pos_id != "" else i
    return posmap


def validate_system_entry(entry: dict[str, str], posmap: dict[POS, int]) -> None:
    pos = get_full_pos_or_none(entry)
    pos_id = get_pos_id(entry)
    if pos is None and pos_id == "":
        raise ValueError(
            f"POS1-POS6 or POS_ID is required for system lexicon row {entry[ROW_INDEX]}")

    if pos is not None and pos_id:
        mapped_pos_id = posmap.get(pos)
        if mapped_pos_id is None or str(mapped_pos_id) != pos_id:
            raise ValueError(
                f"POS_ID does not match POS1-POS6 at system lexicon row {entry[ROW_INDEX]}")


def get_pos(entry: dict[str, str]) -> POS:
    # type: ignore[return-value]
    return tuple(entry.get(c, "") for c in POS_PARTS)


def get_pos_id(entry: dict[str, str]) -> str:
    return entry.get(POS_ID_COLUMN, "").strip()


def get_full_pos_or_none(entry: dict[str, str]) -> POS | None:
    pos = get_pos(entry)
    filled = [part != "" for part in pos]
    if any(filled) and not all(filled):
        raise ValueError(f"Incomplete POS fields at row {entry[ROW_INDEX]}")
    return pos if all(filled) else None


def get_headword(entry: dict[str, str]) -> str:
    headword = entry.get("HEADWORD", "")
    return headword if headword else entry["INDEX_FORM"]


def get_reference_key(entry: dict[str, str]) -> tuple[str, POS, str]:
    return (get_headword(entry), get_pos(entry), entry.get("READING_FORM"))


def build_duplicate_entry_keys(
    entries: list[dict[str, str]],
) -> set[tuple[str, POS, str]]:
    counts: dict[tuple[str, POS, str], int] = {}
    for entry in entries:
        key = get_reference_key(entry)
        counts[key] = counts.get(key, 0) + 1
    return {key for key, count in counts.items() if count > 1}


def get_entry_reference_id(
    entry: dict[str, str],
    duplicate_entry_keys: set[tuple[str, POS, str]],
    *,
    auto_assign: bool,
) -> str:
    reference_id = entry.get(REFERENCE_ID_COLUMN, "")
    if reference_id:
        return reference_id
    if auto_assign and get_reference_key(entry) in duplicate_entry_keys:
        return f"ref-{entry[ROW_INDEX]}"
    return ""


def build_wordref(
    entry: dict[str, str],
    posmap: dict[POS, int],
    duplicate_entry_keys: set[tuple[str, POS, str]],
    *,
    auto_assign_reference_id: bool,
) -> str:
    headword = escape_wordref_part(get_headword(entry))
    reading = escape_wordref_part(entry.get("READING_FORM"))
    reference_id = get_entry_reference_id(
        entry,
        duplicate_entry_keys,
        auto_assign=auto_assign_reference_id,
    )
    escaped_reference_id = escape_wordref_part(reference_id) if reference_id else ""
    pos = get_full_pos_or_none(entry)
    pos_id = get_pos_id(entry)

    if pos_id:
        if escaped_reference_id:
            return f"{headword},{pos_id},{reading},{escaped_reference_id}"
        return f"{headword},{pos_id},{reading}"

    if pos is None:
        raise ValueError(f"POS is required at row {entry[ROW_INDEX]}")

    if pos in posmap:
        if escaped_reference_id:
            return f"{headword},{posmap[pos]},{reading},{escaped_reference_id}"
        return f"{headword},{posmap[pos]},{reading}"

    escaped_pos = [escape_wordref_part(v) for v in pos]
    if escaped_reference_id:
        return ",".join([headword, *escaped_pos, reading, escaped_reference_id])
    return ",".join([headword, *escaped_pos, reading])


def convert_ref(
    ref_string: str,
    input_entries: list[dict[str, str]],
    input_duplicate_entry_keys: set[tuple[str, POS, str]],
    system_entries: list[dict[str, str]] | None,
    posmap: dict[POS, int],
) -> str:
    ref_string = ref_string.strip()
    if ref_string == "":
        return ""

    if NUMERIC_REF_RE.fullmatch(ref_string):
        # input_entries is the lexicon currently being converted, while
        # system_entries is an optional external system lexicon for references.
        is_user_ref = ref_string.startswith("U")
        index = int(ref_string[1:] if is_user_ref else ref_string)
        if is_user_ref:
            if system_entries is None:
                raise ValueError(
                    f"explicit user reference '{ref_string}' is used in a system lexicon")
            target_entries = input_entries
            ref_kind = "user"
        elif system_entries is not None:
            target_entries = system_entries
            ref_kind = "system"
        else:
            target_entries = input_entries
            ref_kind = "input"
        if target_entries is None:
            raise ValueError(
                f"{ref_kind} reference '{ref_string}' requires a corresponding lexicon source")
        if index < 0 or index >= len(target_entries):
            raise IndexError(f"{ref_kind} reference '{ref_string}' is out of range")
        return build_wordref(
            target_entries[index],
            posmap,
            input_duplicate_entry_keys if target_entries is input_entries else set(),
            auto_assign_reference_id=target_entries is input_entries,
        )

    if ref_string.count(WORDREF_DELIMITER) in {2, 3, 7, 8}:
        parts = [unescape(v) for v in ref_string.split(WORDREF_DELIMITER)]
        if len(parts) in {3, 4}:
            out = [
                escape_wordref_part(parts[0]),
                parts[1],
                escape_wordref_part(parts[2]),
            ]
            if len(parts) == 4:
                out.append(escape_wordref_part(parts[3]))
            return ",".join(out)
        headword = escape_wordref_part(parts[0])
        reading = escape_wordref_part(parts[7])
        pos = tuple(parts[1:7])  # type: ignore[assignment]
        escaped_reference_id = (
            escape_wordref_part(parts[8]) if len(parts) == 9 else "")
        if pos in posmap:
            out = [headword, str(posmap[pos]), reading]
            if escaped_reference_id:
                out.append(escaped_reference_id)
            return ",".join(out)
        escaped_pos = [escape_wordref_part(v) for v in parts[1:7]]
        out = [headword, *escaped_pos, reading]
        if escaped_reference_id:
            out.append(escaped_reference_id)
        return ",".join(out)

    raise ValueError(f"Invalid v0 reference: {ref_string}")


def convert_normalized_form(
    normalized: str,
    input_entries: list[dict[str, str]],
    input_duplicate_entry_keys: set[tuple[str, POS, str]],
    system_entries: list[dict[str, str]] | None,
    posmap: dict[POS, int],
) -> str:
    for entry in input_entries:
        if get_headword(entry) == normalized:
            return build_wordref(
                entry,
                posmap,
                input_duplicate_entry_keys,
                auto_assign_reference_id=True,
            )

    if system_entries is not None:
        for entry in system_entries:
            if get_headword(entry) == normalized:
                return build_wordref(
                    entry,
                    posmap,
                    set(),
                    auto_assign_reference_id=False,
                )

    # print as headword-ref (this will be handled as a phantom entry)
    return normalized


def is_self_line_reference(
    ref_string: str,
    entry: dict[str, str],
    *,
    is_system: bool,
) -> bool:
    if not NUMERIC_REF_RE.fullmatch(ref_string):
        return False

    row_index = int(entry[ROW_INDEX])
    if ref_string.startswith("U"):
        return not is_system and int(ref_string[1:]) == row_index
    return is_system and int(ref_string) == row_index


def convert_ref_list(
    value: str,
    input_entries: list[dict[str, str]],
    input_duplicate_entry_keys: set[tuple[str, POS, str]],
    system_entries: list[dict[str, str]] | None,
    posmap: dict[POS, int],
) -> str:
    if value in {"", "*"}:
        return ""

    refs = [
        convert_ref(
            token,
            input_entries,
            input_duplicate_entry_keys,
            system_entries,
            posmap,
        )
        for token in value.split(LIST_DELIMITER)
    ]
    return LIST_DELIMITER.join(refs)


def convert_entry(
    entry: dict[str, str],
    input_entries: list[dict[str, str]],
    input_duplicate_entry_keys: set[tuple[str, POS, str]],
    system_entries: list[dict[str, str]] | None,
    posmap: dict[POS, int],
    pos_id_only: bool,
    drop_leading_zero_synonym_group: bool,
) -> dict[str, str]:
    converted: dict[str, str] = {}

    converted["INDEX_FORM"] = entry["INDEX_FORM"]
    converted["LEFT_ID"] = entry["LEFT_ID"]
    converted["RIGHT_ID"] = entry["RIGHT_ID"]
    converted["COST"] = entry["COST"]

    headword = get_headword(entry)
    converted["HEADWORD"] = "" if headword == entry["INDEX_FORM"] else headword

    pos = get_pos(entry)
    if pos_id_only:
        converted["POS_ID"] = str(
            posmap[pos]) if pos in posmap else entry.get("POS_ID", "")
    else:
        for column in POS_PARTS:
            converted[column] = entry.get(column, "")

    converted["READING_FORM"] = entry.get("READING_FORM")

    converted[REFERENCE_ID_COLUMN] = get_entry_reference_id(
        entry,
        input_duplicate_entry_keys,
        auto_assign=True,
    )

    normalized = entry.get("NORMALIZED_FORM")
    if normalized == headword:
        converted["NORMALIZED_FORM"] = ""
    else:
        converted["NORMALIZED_FORM"] = convert_normalized_form(
            normalized,
            input_entries,
            input_duplicate_entry_keys,
            system_entries,
            posmap,
        )

    dictionary_form = entry.get("DICTIONARY_FORM")
    if dictionary_form == "*" or is_self_line_reference(
        dictionary_form,
        entry,
        is_system=system_entries is None,
    ):
        converted["DICTIONARY_FORM"] = ""
    else:
        converted["DICTIONARY_FORM"] = convert_ref(
            dictionary_form,
            input_entries,
            input_duplicate_entry_keys,
            system_entries,
            posmap,
        )

    for column in ["SPLIT_A", "SPLIT_B", "SPLIT_C", "WORD_STRUCTURE"]:
        converted[column] = convert_ref_list(
            entry.get(column, ""),
            input_entries,
            input_duplicate_entry_keys,
            system_entries,
            posmap,
        )

    synonym_groups = entry.get("SYNONYM_GROUPS", "")
    if synonym_groups == "*":
        converted["SYNONYM_GROUPS"] = ""
    elif drop_leading_zero_synonym_group and synonym_groups:
        parts = synonym_groups.split(LIST_DELIMITER)
        normalized_parts = []
        for part in parts:
            stripped = part.lstrip("0")
            normalized_parts.append(stripped if stripped else "0")
        converted["SYNONYM_GROUPS"] = LIST_DELIMITER.join(normalized_parts)
    else:
        converted["SYNONYM_GROUPS"] = synonym_groups

    converted["USER_DATA"] = entry.get("USER_DATA", "")

    return converted


def write_dictionary_v1(
    output: Path,
    chunk: list[dict[str, str]],
    input_entries: list[dict[str, str]],
    input_duplicate_entry_keys: set[tuple[str, POS, str]],
    system_entries: list[dict[str, str]] | None,
    posmap: dict[POS, int],
    drop_leading_zero_synonym_group: bool,
) -> None:
    pos_id_only = bool(posmap)
    columns = OUTPUT_V1_COLUMNS_WITH_POS_ID if pos_id_only else OUTPUT_V1_COLUMNS_WITH_POS_PARTS

    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", newline="", encoding="utf-8") as fo:
        writer = csv.DictWriter(
            fo, columns, extrasaction="ignore", lineterminator="\n")
        writer.writeheader()
        for entry in tqdm(chunk):
            writer.writerow(
                convert_entry(
                    entry,
                    input_entries,
                    input_duplicate_entry_keys,
                    system_entries,
                    posmap,
                    pos_id_only,
                    drop_leading_zero_synonym_group,
                )
            )


def default_output_path(input_path: Path) -> Path:
    if input_path.suffix:
        return input_path.with_name(f"{input_path.stem}_v1{input_path.suffix}")
    return input_path.with_name(f"{input_path.name}_v1.csv")


def resolve_output_paths(inputs: list[Path], outputs: list[Path] | None) -> list[Path]:
    if outputs is None:
        if len(inputs) == 1:
            return [Path("lexicon_v1.csv")]
        return [default_output_path(path) for path in inputs]

    if len(inputs) == 1 and len(outputs) == 1:
        return outputs

    if len(outputs) == 1 and len(inputs) > 1:
        return [outputs[0] / path.name for path in inputs]

    if len(outputs) == len(inputs):
        return outputs

    raise ValueError(
        "number of output paths must be 1 or match the number of input lexicon files")


def main() -> None:
    args = parse_args()

    posmap = load_pos(args.pos)
    input_entries, entry_chunks = load_csv_rows_from_paths(
        args.lexicon, V0_COLUMNS)
    input_duplicate_entry_keys = build_duplicate_entry_keys(input_entries)
    system_entries = None
    if args.system:
        system_entries, _ = load_csv_rows_from_paths(
            args.system, V1_COLUMNS)
        for entry in system_entries:
            validate_system_entry(entry, posmap)

    output_paths = resolve_output_paths(args.lexicon, args.output)

    for output_path, chunk in zip(output_paths, entry_chunks):
        write_dictionary_v1(
            output_path,
            chunk,
            input_entries,
            input_duplicate_entry_keys,
            system_entries,
            posmap,
            args.drop_leading_zero_synonym_group,
        )


if __name__ == "__main__":
    main()
