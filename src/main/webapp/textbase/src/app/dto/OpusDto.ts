import { AuthorDto } from "./AuthorDto";
import { TeiDivDto } from "./TeiDivDto";

export class OpusDto extends TeiDivDto {
    authors: AuthorDto[]
}