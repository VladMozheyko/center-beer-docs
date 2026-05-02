package fr.mossaab.docmultilang.api;

import fr.mossaab.docmultilang.dto.FileDataDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Files", description = "API для работы с файлами")
public interface FileControllerOpenApi {

    @Operation(
            summary = "Скачать файл по имени",
            description = """
            Возвращает файл по имени.
            Поддерживаются только .pdf и .png.
            Параметр 'type' — строго '.pdf' или '.png'.
            В Content-Disposition выставляется исходное имя файла пользователя.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл найден"),
            @ApiResponse(responseCode = "404", description = "Файл не найден или тип не поддерживается")
    })
    @GetMapping("/by-name/{type}/{fileName}")
    ResponseEntity<Resource> downloadByName(
            @Parameter(description = "Тип файла: .pdf или .png", example = ".pdf")
            @PathVariable String type,

            @Parameter(description = "Техническое имя файла в системе (без расширения)")
            @PathVariable String fileName
    ) throws IOException;

    @Operation(
            summary = "Скачать файл по ID",
            description = """
            Возвращает файл по идентификатору.
            Параметр 'type' — строго '.pdf' или '.png'.
            В Content-Disposition выставляется исходное имя файла пользователя.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл найден"),
            @ApiResponse(responseCode = "404", description = "Файл не найден или тип не поддерживается")
    })
    @GetMapping("/by-id/{id}/{type}")
    ResponseEntity<Resource> downloadById(
            @Parameter(description = "ID файла в базе данных", required = true)
            @PathVariable Long id,

            @Parameter(description = "Тип файла: .pdf или .png", example = ".pdf", required = true)
            @PathVariable String type
    ) throws IOException;

    @Operation(
            summary = "Получить список файлов с пагинацией и фильтрацией",
            description = """
            Возвращает файлы с возможностью фильтрации по типу файла, пагинацией и сортировкой.<br/>
            <ul>
            <li><b>type (строка, необязательный):</b> фильтрация по расширению файла (.pdf, .png)</li>
            <li><b>page (целое, необязательный):</b> номер страницы, начинается с 0</li>
            <li><b>size (целое, необязательный):</b> количество элементов на страницу</li>
            <li><b>sort (строка или массив):</b> поле и направление сортировки, например, createdAt,desc</li>
            </ul>
            <br/>
            Примеры:<br/>
            <code>/api/files/all?type=.pdf&size=10&page=2&sort=createdAt,asc</code>
            <br/>
            <h4>Пример запроса с пагинацией и сортировкой по дате создания и имени</h4><br/>
            <pre><code>{
              "page": 0,
              "size": 1,
              "sort": [
                "createdAt,DESC", "name"
              ]
            }</code></pre>
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список файлов"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры")
    })
    @GetMapping("/all")
    Page<FileDataDTO> listFiles(
            @Parameter(description = "Тип файла для фильтрации (.pdf, .png). Необязательный.")
            @RequestParam(value = "type", required = false) String type,

            @Parameter(description = "Параметры фильтрации, сортировки и пагинации")
            Pageable pageable
    );


    @Operation(
            summary = "Загрузить файл (только PDF или PNG)",
            description = """
            Загружает файл в систему.<br/>
            <ul>
            <li>Форматы: только PDF (.pdf, application/pdf) и PNG (.png, image/png)</li>
            <li>Без указания типа будут возвращаться все файлы</li>
            <li>Ограничения по размеру настраиваются на уровне приложения(Max 10мб)</li>
            </ul>
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл успешно загружен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации формата или размера файла"),
            @ApiResponse(responseCode = "413", description = "Размер файла превышен (Payload Too Large)")
    })
    @PostMapping("/upload")
    ResponseEntity<FileDataDTO> uploadFile(
            @Parameter(
                    description = "Загружаемый файл (PDF или PNG)",
                    required = true
            )
            MultipartFile file
    ) throws Exception;
}
