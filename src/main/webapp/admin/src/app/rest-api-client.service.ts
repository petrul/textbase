import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AdminInfo } from './dto/AdminInfo';
import { TeiRepoDto } from './dto/TeiRepoDto';

@Injectable({
  providedIn: 'root'
})
export class RestApiClientService {

  baseUrl = '/api/admin';
  dataRestUrl = '/api/drest';

  constructor(private http: HttpClient ) {}

  getUrl(str: string): string {
    return `${this.baseUrl}/${str}`;
  }

  getDataRestUrl(str: string): string {
    return `${this.dataRestUrl}/${str}`;
  }

  async getVersion(): Promise<AdminInfo> {
    const url = `${this.baseUrl}/version`;
    const res = await this.http.get<AdminInfo>(url).toPromise();
    console.log(res);
    return res;
  }

  async getAny(str: string) : Promise<any> {
    this.doGet<any>(str);
  }

  async doGet<T>(str: string) : Promise<T> {
    const url = this.getUrl(str);
    const res = await this.http.get<T>(url).toPromise();
    return res;
  }

  async getTeiRepos() : Promise<TeiRepoDto[]> {
    return this.doGet<TeiRepoDto[]>('teirepos');
  }

  async postTeireposReimportAll() {
    const url = this.getUrl('teirepos/reimportAll');
    const res = await this.http.post<string>(url, null).toPromise();
    return res;
  }

  async postTeireposForceReimportAll() {
    const url = this.getUrl('teirepos/forceReimportAll');
    const res = await this.http.post<string>(url, null).toPromise();
    return res;
  }

  async postTeireposReimport(filename: string) {
    const url = this.getUrl(`teirepos/reimport?file=${filename}`);
    const res = await this.http.post<any>(url, null).toPromise();
    return res;
  }


  // data rest
  async getDataRestRelocations() {
    const url = this.getDataRestUrl('relocations/');
    const res = await this.http.get(url).toPromise();
    return res['_embedded']['relocations'];
  }
}
