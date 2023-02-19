import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthorDto } from './dto/AuthorDto';

@Injectable({
  providedIn: 'root'
})
export class RestApiClientService {

  baseUrl = '/api';

  constructor(private http: HttpClient ) {}

  buildUrl(str: string): string {
    return `${this.baseUrl}/${str}`;
  }

  async getAuthors(): Promise<any> {
    const url = this.buildUrl('authors/');
    const res = await this.http.get<AuthorDto[]>(url).toPromise();
    res.forEach( it => {
      it.normalizedName = ("" + it.firstName + " " + it.lastName).normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    })
    console.log(res);
    return res;
  }

  async getAuthor(strid: string): Promise<AuthorDto> {
    const url = this.buildUrl(`authors/${strid}`);
    const res = await this.http.get<AuthorDto>(url).toPromise();
    this.computeNrPages(res);
    console.log(res);
    return res;
  }

  protected computeNrPages(res: AuthorDto) {
    if (res.opera) {
      for (let op of res.opera) {
        if (op.wordSize) {
          op.nrPages = Math.floor(op.wordSize / 250);
        } else if (op.size) {
          op.nrPages = Math.floor(op.size / 2000);
        }
      }
    }
  }

  async getVersion(): Promise<any> {
    const url = this.buildUrl('/admin/version');
    const res = await this.http.get<any>(url).toPromise();
    console.log(res);
    return res;
  }
}
