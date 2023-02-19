import { OpusDto } from "./OpusDto";

export class AuthorDto {

    strId: string;
    firstName: string;
    lastName: string;
    image_href: string;

    opera: OpusDto[];

    normalizedName: string;

}