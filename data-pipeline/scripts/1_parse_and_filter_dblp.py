import gzip
from lxml import etree
import json
import os

# --- CONFIGURATION ---
TARGET_VENUES = {
    # --- Core ML/AI ---
    'NeurIPS', 'NIPS', 'ICML', 'ICLR', 'AAAI', 'IJCAI',
    
    # --- Computer Vision ---
    'CVPR', 'ICCV', 'ECCV',
    
    # --- Natural Language Processing ---
    'ACL', 'EMNLP', 'NAACL',
    
    # --- Databases (DB) ---
    'SIGMOD Conference','ICDE',

    # --- Specialized AI/Stats ---
    # 'AISTATS',
    # 'UAI',

    # --- Data Mining (DM) / Information Retrieval (IR) ---
    'KDD',
    'WSDM',     
    # 'SIGIR',
    # 'CIKM',
    # 'WWW',
    # 'ICDM',
}

def clean_text(text):
    """
    Removes specific Unicode line terminators that break JSONL format
    and text editors (U+2028, U+2029).
    """
    if not text:
        return None
    # Replace Line Separator and Paragraph Separator with a standard space
    return text.replace('\u2028', ' ').replace('\u2029', ' ').strip()

def parse_and_filter_dblp(xml_path, output_path, start_year=2015, end_year=2020):
    publication_tags = {'article', 'inproceedings', 'incollection', 'book'}
    
    dtd_dir = os.path.dirname(xml_path) or '.'
    dtd_path = os.path.join(dtd_dir, 'dblp.dtd')
    
    if not os.path.exists(dtd_path):
        print(f"WARNING: 'dblp.dtd' not found at {dtd_path}")
        print("Parsing will likely fail on special characters.")
    
    print(f"Processing {xml_path}...")

    try:
        file_obj = gzip.open(xml_path, 'rb')
        # Using ISO-8859-1 to match DBLP encoding and avoid utf-8 errors
        context = etree.iterparse(
            file_obj, 
            events=('end',), 
            load_dtd=True, 
            encoding='ISO-8859-1', 
            huge_tree=True
        )
    except Exception as e:
        print(f"Error opening file: {e}")
        return

    count = 0
    skipped_no_doi = 0
    skipped_wrong_venue = 0 
    
    with open(output_path, 'w', encoding='utf-8') as outfile:
        for event, elem in context:
            if elem.tag in publication_tags:
                
                year_text = elem.findtext('year')
                try:
                    year = int(year_text) if year_text else 0
                except (ValueError, TypeError):
                    year = 0

                if start_year <= year <= end_year:
                    
                    # Clean venue text immediately
                    venue_raw = elem.findtext('journal') or elem.findtext('booktitle')
                    venue = clean_text(venue_raw)
                    
                    if venue and venue in TARGET_VENUES:

                        ee_tags = elem.findall('ee')
                        doi = None
                        
                        for ee in ee_tags:
                            if ee.text and 'doi.org/' in ee.text:
                                doi = ee.text.split('doi.org/')[-1]
                                break
                        
                        if doi:
                            # --- CLEANING DATA HERE ---
                            title = clean_text(elem.findtext('title'))
                            
                            # Extract and clean all authors
                            authors = []
                            for author in elem.findall('author'):
                                cleaned_name = clean_text(author.text)
                                if cleaned_name:
                                    authors.append(cleaned_name)

                            record = {
                                'dblp_key': elem.get('key'),
                                'title': title,
                                'year': year,
                                'doi': doi,
                                'authors': authors,
                                'venue': venue
                            }
                            
                            outfile.write(json.dumps(record, ensure_ascii=False) + '\n')
                            count += 1
                        else:
                            skipped_no_doi += 1
                    else:
                        skipped_wrong_venue += 1

                elem.clear()
                while elem.getprevious() is not None:
                    del elem.getparent()[0]
                    
    file_obj.close()
    print(f"Phase 1 Complete.")
    print(f"Saved {count} records to {output_path}")

if __name__ == "__main__":
    INPUT_XML = 'dblp.xml.gz'
    START_YEAR = 2015
    END_YEAR = 2025
    OUTPUT_JSONL = f'dblp_ai_ml_{START_YEAR}_{END_YEAR}.jsonl'

    print("--- Phase 1: Parsing and Filtering DBLP ---")
    
    if os.path.exists(INPUT_XML):
        parse_and_filter_dblp(INPUT_XML, OUTPUT_JSONL, start_year=START_YEAR, end_year=END_YEAR)
    else:
        print("Input file not found.")