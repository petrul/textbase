export class TeiDivDto {
    head: string;
    path: string;
    parent: TeiDivDto;    
    depth: number;
    url: string;
    size:number; // size in chars
    wordSize:number; // size in words
    nrPages:number;
}